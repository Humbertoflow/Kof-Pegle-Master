package com.example.peggle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.peggle.audio.PeggleAudioEngine
import com.example.peggle.audio.SoundManager
import com.example.peggle.data.GamePreferences
import com.example.peggle.data.LevelCatalog
import com.example.peggle.data.LevelData
import com.example.peggle.data.LevelManager
import com.example.peggle.model.Ball
import com.example.peggle.model.Bucket
import com.example.peggle.model.GameMode
import com.example.peggle.model.Particle
import com.example.peggle.model.ParticleType
import com.example.peggle.model.Peg
import com.example.peggle.model.PegShape
import com.example.peggle.model.PegType
import com.example.peggle.model.PeggleMaster
import com.example.peggle.model.ScoreBin
import com.example.peggle.model.ScoreFloater
import com.example.peggle.model.ShotPhase
import com.example.peggle.model.ShotStatistics
import com.example.peggle.physics.PegglePhysicsEngine
import com.example.peggle.vibration.PeggleHaptics
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

data class PeggleUiState(
    val levelData: LevelData = LevelCatalog.getAllLevels().first(),
    val gameMode: GameMode = GameMode.ADVENTURE,
    val selectedMaster: PeggleMaster = PeggleMaster.BJORN,
    val shotPhase: ShotPhase = ShotPhase.AIMING,
    val currentScore: Int = 0,
    val highscore: Int = 0,
    val ballsRemaining: Int = 10,
    val orangePegsRemaining: Int = 25,
    val totalOrangePegs: Int = 25,
    val cannonAngleDeg: Float = 90f, // 90 is straight down
    val isFastForward: Boolean = false,
    val isPaused: Boolean = false,
    val showVictoryDialog: Boolean = false,
    val showGameOverDialog: Boolean = false,
    val earnedStars: Int = 0,
    val isMasterClear: Boolean = false,
    val feverBinScoreAwarded: Int = 0,
    val duelPlayer1Score: Int = 0,
    val duelPlayer2Score: Int = 0,
    val duelCurrentTurnPlayer: Int = 1,
    val lastSpecialPowerMessage: String? = null,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
)

class PeggleViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = GamePreferences(application)
    val levelManager = LevelManager.getInstance(application)
    val soundManager = SoundManager.getInstance(application)
    val audioEngine = PeggleAudioEngine()
    val haptics = PeggleHaptics(application)
    val physicsEngine = PegglePhysicsEngine()

    private val _uiState = MutableStateFlow(PeggleUiState())
    val uiState: StateFlow<PeggleUiState> = _uiState.asStateFlow()

    // Game entity states
    val pegs = mutableListOf<Peg>()
    var ball: Ball? = null
    val extraBalls = mutableListOf<Ball>()
    val bucket = Bucket(x = 200f, y = 620f)
    val scoreFloaters = mutableListOf<ScoreFloater>()
    val particles = mutableListOf<Particle>()
    var trajectoryPoints = listOf<PegglePhysicsEngine.TrajectoryPoint>()

    val scoreBins = listOf(
        ScoreBin(0, "10,000", 10000, 0.00f, 0.20f, 0xFF4A90E2),
        ScoreBin(1, "50,000", 50000, 0.20f, 0.40f, 0xFFFF8A00),
        ScoreBin(2, "100,000", 100000, 0.40f, 0.60f, 0xFFFFD700),
        ScoreBin(3, "50,000", 50000, 0.60f, 0.80f, 0xFFFF8A00),
        ScoreBin(4, "10,000", 10000, 0.80f, 1.00f, 0xFF4A90E2)
    )

    private var boardWidth: Float = 400f
    private var boardHeight: Float = 660f
    private var gameLoopJob: Job? = null
    private var nextFloaterId: Long = 1L
    private var currentShotStats = ShotStatistics()
    private var superGuideActiveForShots: Int = 0
    private var scoreMultiplierBuff: Int = 1

    init {
        val soundPref = preferences.isSoundEnabled()
        val hapticPref = preferences.isHapticsEnabled()
        soundManager.soundEnabled = soundPref
        audioEngine.soundEnabled = soundPref
        haptics.hapticsEnabled = hapticPref
        _uiState.update { it.copy(soundEnabled = soundPref, hapticsEnabled = hapticPref) }
        viewModelScope.launch {
            levelManager.ensureDefaultLevelsSeeded()
        }
    }

    fun setBoardDimensions(width: Float, height: Float) {
        if (width <= 0f || height <= 0f) return
        val changed = (boardWidth != width || boardHeight != height)
        val oldW = boardWidth
        val oldH = boardHeight
        boardWidth = width
        boardHeight = height
        bucket.y = height - 28f
        bucket.x = (bucket.x * (width / oldW)).coerceIn(bucket.effectiveWidth / 2f, width - bucket.effectiveWidth / 2f)

        if (pegs.isEmpty()) {
            loadLevel(_uiState.value.levelData, _uiState.value.selectedMaster)
        } else if (changed && _uiState.value.shotPhase == ShotPhase.AIMING && _uiState.value.currentScore == 0) {
            loadLevel(_uiState.value.levelData, _uiState.value.selectedMaster)
        } else if (changed) {
            val scaleX = width / oldW
            val scaleY = height / oldH
            pegs.forEach { peg ->
                peg.x *= scaleX
                peg.y *= scaleY
            }
            updateTrajectory()
        }
    }

    fun loadLevel(level: LevelData, master: PeggleMaster = level.master) {
        soundManager.stopFeverMelody()
        audioEngine.stopFeverMelody()
        pegs.clear()
        pegs.addAll(levelManager.instantiatePegs(level, boardWidth, boardHeight))

        ball = null
        extraBalls.clear()
        scoreFloaters.clear()
        particles.clear()
        currentShotStats = ShotStatistics()
        superGuideActiveForShots = 0
        scoreMultiplierBuff = 1
        bucket.hasPyramidWings = false

        val orangeCount = pegs.count { it.type == PegType.ORANGE }
        assignRandomPurplePeg()

        val hs = preferences.getLevelHighScore(level.id)

        _uiState.update {
            it.copy(
                levelData = level,
                selectedMaster = master,
                shotPhase = ShotPhase.AIMING,
                currentScore = 0,
                highscore = hs,
                ballsRemaining = 10,
                orangePegsRemaining = orangeCount,
                totalOrangePegs = orangeCount,
                cannonAngleDeg = 90f,
                showVictoryDialog = false,
                showGameOverDialog = false,
                earnedStars = 0,
                isMasterClear = false,
                lastSpecialPowerMessage = null,
                feverBinScoreAwarded = 0
            )
        }

        viewModelScope.launch {
            val config = levelManager.getLevelById(level.id)
            if (config != null) {
                _uiState.update { current ->
                    if (current.levelData.id == level.id && current.currentScore == 0 && current.shotPhase == ShotPhase.AIMING) {
                        current.copy(ballsRemaining = config.initialBallCount)
                    } else current
                }
            }
        }

        updateTrajectory()
        startGameLoop()
    }

    fun restartCurrentLevel() {
        loadLevel(_uiState.value.levelData, _uiState.value.selectedMaster)
    }

    fun selectMaster(master: PeggleMaster) {
        _uiState.update { it.copy(selectedMaster = master) }
        updateTrajectory()
    }

    fun setGameMode(mode: GameMode) {
        _uiState.update {
            it.copy(
                gameMode = mode,
                duelPlayer1Score = 0,
                duelPlayer2Score = 0,
                duelCurrentTurnPlayer = 1
            )
        }
    }

    fun setCannonAngle(angleDeg: Float) {
        if (_uiState.value.shotPhase != ShotPhase.AIMING) return
        val clamped = angleDeg.coerceIn(18f, 162f)
        _uiState.update { it.copy(cannonAngleDeg = clamped) }
        updateTrajectory()
    }

    fun nudgeAngle(deltaDeg: Float) {
        if (_uiState.value.shotPhase != ShotPhase.AIMING) return
        val newAngle = (_uiState.value.cannonAngleDeg + deltaDeg).coerceIn(18f, 162f)
        _uiState.update { it.copy(cannonAngleDeg = newAngle) }
        haptics.tick()
        updateTrajectory()
    }

    fun toggleFastForward() {
        _uiState.update { it.copy(isFastForward = !it.isFastForward) }
    }

    fun toggleSound() {
        val next = !_uiState.value.soundEnabled
        preferences.setSoundEnabled(next)
        soundManager.soundEnabled = next
        audioEngine.soundEnabled = next
        _uiState.update { it.copy(soundEnabled = next) }
    }

    fun toggleHaptics() {
        val next = !_uiState.value.hapticsEnabled
        preferences.setHapticsEnabled(next)
        haptics.hapticsEnabled = next
        _uiState.update { it.copy(hapticsEnabled = next) }
    }

    fun togglePause() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }

    private fun updateTrajectory() {
        val angleRad = Math.toRadians(_uiState.value.cannonAngleDeg.toDouble()).toFloat()
        val startX = boardWidth / 2f
        val startY = 32f
        val speed = 640f
        val isSuperGuide = (superGuideActiveForShots > 0) || (_uiState.value.selectedMaster == PeggleMaster.BJORN && currentShotStats.greenHitInShot > 0)

        trajectoryPoints = physicsEngine.calculateTrajectory(
            startX = startX,
            startY = startY,
            angleRad = angleRad,
            speed = speed,
            pegs = pegs,
            boardWidth = boardWidth,
            boardHeight = boardHeight,
            superGuide = isSuperGuide
        )
    }

    fun fireBall() {
        val state = _uiState.value
        if (state.shotPhase != ShotPhase.AIMING || state.ballsRemaining <= 0) return

        val angleRad = Math.toRadians(state.cannonAngleDeg.toDouble()).toFloat()
        val muzzleX = boardWidth / 2f
        val muzzleY = 32f
        val speed = 640f

        ball = Ball(
            x = muzzleX,
            y = muzzleY,
            vx = cos(angleRad) * speed,
            vy = sin(angleRad) * speed,
            active = true
        )

        currentShotStats = ShotStatistics()
        scoreMultiplierBuff = 1

        soundManager.playCannonFire()
        audioEngine.playCannonFire()
        haptics.cannonShot()

        _uiState.update {
            it.copy(
                shotPhase = ShotPhase.BALL_IN_PLAY,
                ballsRemaining = it.ballsRemaining - 1
            )
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.currentTimeMillis()
            while (isActive) {
                val now = System.currentTimeMillis()
                val realDt = ((now - lastTime) / 1000f).coerceIn(0.001f, 0.05f)
                lastTime = now

                if (!_uiState.value.isPaused) {
                    val speedFactor = when (_uiState.value.shotPhase) {
                        ShotPhase.FEVER_SLOWMO -> 0.30f
                        else -> if (_uiState.value.isFastForward) 2.0f else 1.0f
                    }
                    val dt = realDt * speedFactor
                    updatePhysics(dt)
                }

                delay(16) // ~60fps target
            }
        }
    }

    private fun updatePhysics(dt: Float) {
        val currentBall = ball
        val state = _uiState.value

        // Update bucket movement
        physicsEngine.updateBucket(bucket, boardWidth, dt)

        // Update score floaters
        val floaterIter = scoreFloaters.iterator()
        while (floaterIter.hasNext()) {
            val f = floaterIter.next()
            f.y -= 38f * dt
            f.alpha -= 0.8f * dt
            if (f.alpha <= 0f) {
                floaterIter.remove()
            }
        }

        // Update particle physics
        val partIter = particles.iterator()
        while (partIter.hasNext()) {
            val p = partIter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.rotation += p.vRot * dt
            if (p.growthRate != 0f) {
                p.size += p.growthRate * dt
            }

            when (p.type) {
                ParticleType.SHOCKWAVE_RING -> {
                    p.life -= 3.2f * dt
                }
                ParticleType.GLOW_EMBER -> {
                    p.vy -= 90f * dt // buoyant upward flame drift
                    p.vx *= (1f - 1.2f * dt).coerceAtLeast(0f)
                    p.life -= 1.1f * dt
                }
                ParticleType.STAR_SPARKLE -> {
                    p.vx *= (1f - 2.0f * dt).coerceAtLeast(0f)
                    p.vy *= (1f - 2.0f * dt).coerceAtLeast(0f)
                    p.life -= 1.6f * dt
                }
                ParticleType.SPARK -> {
                    p.vx *= (1f - 1.2f * dt).coerceAtLeast(0f)
                    p.vy += 380f * dt
                    p.life -= 1.6f * dt
                }
            }

            if (p.life <= 0f) {
                partIter.remove()
            }
        }

        // Active ball updates (main ball + any multiball clones)
        val allBalls = mutableListOf<Ball>()
        if (currentBall != null && currentBall.active) {
            allBalls.add(currentBall)
        }
        allBalls.addAll(extraBalls.filter { it.active })

        for (activeB in allBalls) {
            val caught = physicsEngine.update(
                ball = activeB,
                pegs = pegs,
                bucket = bucket,
                boardWidth = boardWidth,
                boardHeight = boardHeight,
                dt = dt,
                onPegHit = { peg -> handlePegHit(peg, activeB) },
                onRimBounce = {
                    haptics.tick()
                    addStyleShot("¡Rebote en el Cubo!")
                }
            )

            if (caught) {
                handleBucketCatch(activeB)
            }

            // Ball fell off screen
            if (activeB.y > boardHeight + 40f) {
                if (activeB.spookyRespawnsLeft > 0) {
                    // Renfield Spooky Ball power triggers!
                    activeB.spookyRespawnsLeft--
                    activeB.y = 20f
                    activeB.vy = 260f
                    activeB.vx = (Random.nextFloat() - 0.5f) * 200f
                    audioEngine.playPowerActivated()
                    addFloater("¡BOLA FANTASMA!", activeB.x, 60f, 0xFFFF8A00, isStyle = true)
                    haptics.tick()
                } else {
                    activeB.active = false
                }
            }
        }

        // Check if all active balls have finished their flight
        val anyStillActive = (currentBall?.active == true) || extraBalls.any { it.active }
        val currentPhase = _uiState.value.shotPhase
        if (!anyStillActive && (currentBall != null && !currentBall.active) &&
            (currentPhase == ShotPhase.BALL_IN_PLAY || currentPhase == ShotPhase.FEVER_SLOWMO)) {
            onBallFinished()
        }
    }

    private fun handlePegHit(peg: Peg, activeBall: Ball) {
        if (peg.isHit) return // Already hit during this shot

        peg.isHit = true
        peg.hitTimeMs = System.currentTimeMillis()
        currentShotStats.pegsHitInShot++
        peg.hitOrder = currentShotStats.pegsHitInShot

        val multiplier = getFeverMultiplier() * scoreMultiplierBuff
        var pegBasePoints = when (peg.type) {
            PegType.BLUE -> 10
            PegType.ORANGE -> 100
            PegType.GREEN -> 10
            PegType.PURPLE -> 500
            PegType.MULTIBALL -> 250
            PegType.LONG_AIM -> 250
            PegType.BLAST_RADIUS -> 250
        }

        val earnedPoints = pegBasePoints * multiplier
        currentShotStats.shotScore += earnedPoints

        // Sound and Haptics
        soundManager.playPegHit(
            hitCountInShot = currentShotStats.pegsHitInShot,
            pegType = peg.type
        )
        audioEngine.playPegHit(
            hitCountInShot = currentShotStats.pegsHitInShot,
            isOrange = peg.type == PegType.ORANGE,
            isPurple = peg.type == PegType.PURPLE
        )
        haptics.tick()

        // Spawn floater
        val colorHex = when (peg.type) {
            PegType.BLUE -> 0xFF4A90E2
            PegType.ORANGE -> 0xFFFF8A00
            PegType.GREEN -> 0xFF34C759
            PegType.PURPLE -> 0xFFBF5AF2
            PegType.MULTIBALL -> 0xFF00E5FF
            PegType.LONG_AIM -> 0xFFFFD700
            PegType.BLAST_RADIUS -> 0xFFFF3B30
        }
        addFloater("+$earnedPoints", peg.x, peg.y - 12f, colorHex)

        // Spawn dynamic particle animation effects on peg hit
        spawnPegHitParticles(
            peg = peg,
            impactX = (peg.x + activeBall.x) / 2f,
            impactY = (peg.y + activeBall.y) / 2f,
            comboCount = currentShotStats.pegsHitInShot
        )

        // Count special pegs
        when (peg.type) {
            PegType.ORANGE -> {
                currentShotStats.orangeHitInShot++
                val remaining = pegs.count { it.type == PegType.ORANGE && !it.isHit && !it.isRemoved }
                _uiState.update { it.copy(orangePegsRemaining = remaining) }

                // Check for last orange peg dramatic slow-mo
                if (remaining == 0 && _uiState.value.shotPhase != ShotPhase.FEVER_SLOWMO && _uiState.value.shotPhase != ShotPhase.EXTREME_FEVER) {
                    triggerExtremeFeverSlowMo()
                }
            }
            PegType.GREEN -> {
                currentShotStats.greenHitInShot++
                triggerMasterPower(activeBall)
            }
            PegType.PURPLE -> {
                currentShotStats.purpleHitInShot++
                addStyleShot("¡Súper Púrpura!")
            }
            PegType.MULTIBALL -> {
                currentShotStats.multiballHitInShot++
                triggerMultiballPower(activeBall)
            }
            PegType.LONG_AIM -> {
                currentShotStats.longAimHitInShot++
                triggerLongAimPower()
            }
            PegType.BLAST_RADIUS -> {
                currentShotStats.blastRadiusHitInShot++
                triggerBlastRadiusPower(peg.x, peg.y, activeBall)
            }
            PegType.BLUE -> {}
        }

        // Style Shot Checks
        if (currentShotStats.pegsHitInShot == 10) {
            addStyleShot("¡Súper Combo!")
            soundManager.playKofHeavyImpact()
        } else if (currentShotStats.pegsHitInShot == 20) {
            addStyleShot("¡Mega Combo!")
            soundManager.playKofHeavyImpact()
        }

        // Free ball by shot points threshold (25k, 75k, 125k)
        if (!currentShotStats.freeBallAwardedByPoints && currentShotStats.shotScore >= 25000) {
            currentShotStats.freeBallAwardedByPoints = true
            _uiState.update { it.copy(ballsRemaining = it.ballsRemaining + 1) }
            addFloater("¡BOLA EXTRA (+25k pts)!", boardWidth / 2f, 120f, 0xFFFFD700, isStyle = true)
            soundManager.playBucketCatch()
            audioEngine.playFreeBallBucketCatch()
            haptics.bucketCatch()
        }
    }

    private fun triggerMasterPower(activeBall: Ball) {
        val master = _uiState.value.selectedMaster
        soundManager.playKofSuperFlash()
        audioEngine.playPowerActivated()
        haptics.click()

        when (master) {
            PeggleMaster.BJORN -> {
                superGuideActiveForShots = 3
                _uiState.update { it.copy(lastSpecialPowerMessage = "¡Super Guía Activada (3 Tiros)!") }
                addFloater("¡SUPER GUÍA!", boardWidth / 2f, 100f, 0xFF4A90E2, isStyle = true)
            }
            PeggleMaster.RENFIELD -> {
                activeBall.spookyRespawnsLeft = 1
                _uiState.update { it.copy(lastSpecialPowerMessage = "¡Bola Fantasma Armada!") }
                addFloater("¡BOLA FANTASMA!", boardWidth / 2f, 100f, 0xFFFF8A00, isStyle = true)
            }
            PeggleMaster.KAT_TUT -> {
                bucket.hasPyramidWings = true
                _uiState.update { it.copy(lastSpecialPowerMessage = "¡Alas Piramidales Desplegadas!") }
                addFloater("¡ALAS PIRAMIDALES!", boardWidth / 2f, 100f, 0xFFFFD700, isStyle = true)
            }
            PeggleMaster.MASTER_SPLODER -> {
                detonateExplosion(activeBall.x, activeBall.y)
                _uiState.update { it.copy(lastSpecialPowerMessage = "¡Explosión de Magma!") }
                addFloater("¡BOOOM!", activeBall.x, activeBall.y - 20f, 0xFFFF3B30, isStyle = true)
            }
            PeggleMaster.WARREN -> {
                val spinResult = Random.nextInt(3)
                when (spinResult) {
                    0 -> {
                        scoreMultiplierBuff = 3
                        _uiState.update { it.copy(lastSpecialPowerMessage = "¡Ruleta: Triple Puntuación (3x)!") }
                        addFloater("¡3X PUNTOS!", boardWidth / 2f, 100f, 0xFF9B51E0, isStyle = true)
                    }
                    1 -> {
                        _uiState.update {
                            it.copy(
                                ballsRemaining = it.ballsRemaining + 2,
                                lastSpecialPowerMessage = "¡Ruleta: +2 Bolas Extra!"
                            )
                        }
                        addFloater("¡+2 BOLAS!", boardWidth / 2f, 100f, 0xFF9B51E0, isStyle = true)
                    }
                    else -> {
                        detonateExplosion(activeBall.x, activeBall.y)
                        _uiState.update { it.copy(lastSpecialPowerMessage = "¡Ruleta: Sombrero Explosivo!") }
                        addFloater("¡TRUCO MÁGICO!", boardWidth / 2f, 100f, 0xFF9B51E0, isStyle = true)
                    }
                }
            }
        }
    }

    private fun triggerMultiballPower(originBall: Ball) {
        soundManager.playKofSuperFlash()
        audioEngine.playPowerActivated()
        haptics.click()

        val speed = hypot(originBall.vx, originBall.vy).coerceAtLeast(320f)
        val currentAngle = atan2(originBall.vy.toDouble(), originBall.vx.toDouble()).toFloat()

        // Create 2 clone balls fanning out at +/- 25 degrees
        val angle1 = currentAngle - 0.44f
        val angle2 = currentAngle + 0.44f

        val clone1 = Ball(
            id = extraBalls.size + 1,
            x = originBall.x,
            y = originBall.y,
            vx = cos(angle1) * speed,
            vy = sin(angle1) * speed,
            active = true,
            isMultiballClone = true
        )
        val clone2 = Ball(
            id = extraBalls.size + 2,
            x = originBall.x,
            y = originBall.y,
            vx = cos(angle2) * speed,
            vy = sin(angle2) * speed,
            active = true,
            isMultiballClone = true
        )

        extraBalls.add(clone1)
        extraBalls.add(clone2)

        _uiState.update { it.copy(lastSpecialPowerMessage = "¡MULTIBOLA ACTIVADA (3X ESFERAS)!") }
        addFloater("¡MULTIBOLA!", originBall.x, originBall.y - 20f, 0xFF00E5FF, isStyle = true)
        addStyleShot("¡Poder Multibola!")
    }

    private fun triggerLongAimPower() {
        soundManager.playKofSuperFlash()
        audioEngine.playPowerActivated()
        haptics.click()

        superGuideActiveForShots = (superGuideActiveForShots + 3).coerceAtLeast(3)
        _uiState.update { it.copy(lastSpecialPowerMessage = "¡PUNTERÍA EXTENDIDA (MIRA LÁSER 4 REBOTES)!") }
        addFloater("¡PUNTERÍA LARGA!", boardWidth / 2f, 100f, 0xFFFFD700, isStyle = true)
        addStyleShot("¡Mira Láser Extendida!")
        updateTrajectory()
    }

    private fun triggerBlastRadiusPower(centerX: Float, centerY: Float, originBall: Ball) {
        soundManager.playKofFlameBurst()
        soundManager.playKofHeavyImpact()
        audioEngine.playPowerActivated()
        haptics.extremeFeverCelebration()

        val blastRadius = 95f
        spawnExplosionParticles(centerX, centerY, 0xFFFF3B30)
        spawnExplosionParticles(centerX, centerY, 0xFFFF8A00)

        // Detonate neighboring pegs
        pegs.forEach { targetPeg ->
            if (!targetPeg.isHit && !targetPeg.isRemoved) {
                val dist = hypot(targetPeg.x - centerX, targetPeg.y - centerY)
                if (dist <= blastRadius) {
                    handlePegHit(targetPeg, originBall)
                }
            }
        }

        _uiState.update { it.copy(lastSpecialPowerMessage = "¡ONDA EXPANSIVA DETONADA!") }
        addFloater("¡BLAST RADIUS!", centerX, centerY - 25f, 0xFFFF2A2A, isStyle = true)
        addStyleShot("¡Onda Explosiva!")
    }

    private fun detonateExplosion(centerX: Float, centerY: Float) {
        val explosionRadius = 75f
        spawnExplosionParticles(centerX, centerY, 0xFFFF4500)
        pegs.forEach { peg ->
            if (!peg.isHit && !peg.isRemoved) {
                val dist = hypot(peg.x - centerX, peg.y - centerY)
                if (dist <= explosionRadius) {
                    handlePegHit(peg, ball ?: return@forEach)
                }
            }
        }
    }

    private fun handleBucketCatch(caughtBall: Ball? = null) {
        currentShotStats.caughtInBucket = true
        bucket.catchAnimTimer = 0.5f
        _uiState.update { it.copy(ballsRemaining = it.ballsRemaining + 1) }
        spawnBucketCatchCelebration(bucket.x, bucket.y - 15f)
        soundManager.playBucketCatch()
        audioEngine.playFreeBallBucketCatch()
        haptics.bucketCatch()
        addFloater("¡BOLA GRATIS!", bucket.x, bucket.y - 30f, 0xFFFFD700, isStyle = true)
        if (caughtBall != null) {
            caughtBall.active = false
        } else {
            ball?.active = false
        }

        val anyStillActive = (ball?.active == true) || extraBalls.any { it.active }
        val currentPhase = _uiState.value.shotPhase
        if (!anyStillActive && (currentPhase == ShotPhase.BALL_IN_PLAY || currentPhase == ShotPhase.FEVER_SLOWMO)) {
            onBallFinished()
        }
    }

    private fun onBallFinished() {
        val currentPhase = _uiState.value.shotPhase
        if (currentPhase == ShotPhase.RESOLVING || currentPhase == ShotPhase.EXTREME_FEVER) return
        _uiState.update { it.copy(shotPhase = ShotPhase.RESOLVING) }

        viewModelScope.launch {
            // Popping lit pegs
            val hitPegs = pegs.filter { it.isHit && !it.isRemoved }
            if (hitPegs.isNotEmpty()) {
                soundManager.playPegPop()
            }
            for (peg in hitPegs) {
                peg.isRemoved = true
                spawnExplosionParticles(peg.x, peg.y, getPegColor(peg.type))
            }

            // Update scores
            val shotScore = currentShotStats.shotScore
            val newScore = _uiState.value.currentScore + shotScore

            if (_uiState.value.gameMode == GameMode.DUEL) {
                if (_uiState.value.duelCurrentTurnPlayer == 1) {
                    _uiState.update {
                        it.copy(
                            currentScore = newScore,
                            duelPlayer1Score = it.duelPlayer1Score + shotScore,
                            duelCurrentTurnPlayer = 2
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            currentScore = newScore,
                            duelPlayer2Score = it.duelPlayer2Score + shotScore,
                            duelCurrentTurnPlayer = 1
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(currentScore = newScore) }
            }

            if (superGuideActiveForShots > 0) {
                superGuideActiveForShots--
            }

            delay(400) // Brief pause for pop satisfaction

            // Check if Extreme Fever triggered
            val remainingOrange = pegs.count { it.type == PegType.ORANGE && !it.isRemoved }
            if (remainingOrange == 0) {
                triggerExtremeFeverCelebration()
            } else if (_uiState.value.ballsRemaining <= 0) {
                // Out of balls!
                _uiState.update { it.copy(showGameOverDialog = true) }
            } else {
                // Next shot ready
                assignRandomPurplePeg()
                _uiState.update { it.copy(shotPhase = ShotPhase.AIMING) }
                updateTrajectory()
            }
        }
    }

    private fun triggerExtremeFeverSlowMo() {
        _uiState.update { it.copy(shotPhase = ShotPhase.FEVER_SLOWMO) }
        soundManager.playKofFlameBurst()
        haptics.extremeFeverCelebration()
    }

    private fun triggerExtremeFeverCelebration() {
        viewModelScope.launch {
            _uiState.update { it.copy(shotPhase = ShotPhase.EXTREME_FEVER) }
            soundManager.playExtremeFeverOdeToJoy()
            audioEngine.playExtremeFeverOdeToJoy()
            haptics.extremeFeverCelebration()

            // Bonus for leftover balls (10k per ball)
            val leftoverBalls = _uiState.value.ballsRemaining
            val ballBonus = leftoverBalls * 10000

            // Simulate ball falling into highest score bin in center or random bin
            val targetBin = scoreBins[2] // 100,000 pts center bin!
            val totalFeverScore = _uiState.value.currentScore + ballBonus + targetBin.points

            // Launch fireworks
            repeat(8) {
                val fx = Random.nextFloat() * boardWidth
                val fy = 60f + Random.nextFloat() * (boardHeight * 0.5f)
                spawnFireworks(fx, fy)
                delay(300)
            }

            val isAllPegsCleared = pegs.none { !it.isRemoved }
            val stars = calculateStars(totalFeverScore)

            preferences.setLevelHighScore(_uiState.value.levelData.id, totalFeverScore)
            preferences.setLevelStars(_uiState.value.levelData.id, stars)
            preferences.setLevelCleared(_uiState.value.levelData.id)
            if (isAllPegsCleared) {
                preferences.setMasterClear(_uiState.value.levelData.id)
            }

            soundManager.playKofKoVictory()
            _uiState.update {
                it.copy(
                    currentScore = totalFeverScore,
                    feverBinScoreAwarded = targetBin.points,
                    earnedStars = stars,
                    isMasterClear = isAllPegsCleared,
                    showVictoryDialog = true
                )
            }
        }
    }

    private fun calculateStars(score: Int): Int {
        return when {
            score >= 250000 -> 3
            score >= 150000 -> 2
            else -> 1
        }
    }

    private fun assignRandomPurplePeg() {
        // Reset old purple peg to blue
        pegs.forEach {
            if (it.type == PegType.PURPLE) {
                it.type = PegType.BLUE
            }
        }

        // Pick an unhit blue peg to become purple
        val candidates = pegs.filter { !it.isHit && !it.isRemoved && it.type == PegType.BLUE }
        if (candidates.isNotEmpty()) {
            val picked = candidates.random()
            picked.type = PegType.PURPLE
        }
    }

    fun getFeverMultiplier(): Int {
        val remaining = _uiState.value.orangePegsRemaining
        return when {
            remaining <= 3 -> 10
            remaining <= 7 -> 5
            remaining <= 14 -> 3
            remaining <= 19 -> 2
            else -> 1
        }
    }

    private fun addFloater(text: String, x: Float, y: Float, color: Long, isStyle: Boolean = false) {
        scoreFloaters.add(
            ScoreFloater(
                id = nextFloaterId++,
                text = text,
                x = x.coerceIn(20f, boardWidth - 20f),
                y = y,
                color = color,
                isStyleShot = isStyle
            )
        )
    }

    private fun addStyleShot(name: String) {
        currentShotStats.styleShots.add(name)
        val styleBonus = 2500
        currentShotStats.shotScore += styleBonus
        addFloater(name, boardWidth / 2f, 140f + currentShotStats.styleShots.size * 22f, 0xFFFFD700, isStyle = true)
        haptics.tick()
    }

    private fun spawnPegHitParticles(peg: Peg, impactX: Float, impactY: Float, comboCount: Int) {
        val pegColor = getPegColor(peg.type)
        val isOrange = peg.type == PegType.ORANGE
        val isGreen = peg.type == PegType.GREEN
        val isPurple = peg.type == PegType.PURPLE

        // 1. Shockwave ring expanding from struck peg
        val ringColor = when (peg.type) {
            PegType.BLUE -> 0xFF00E5FF // Neon Cyan
            PegType.ORANGE -> 0xFFFF6D00 // Blazing Orange
            PegType.GREEN -> 0xFF00E676 // Emerald
            PegType.PURPLE -> 0xFFE040FB // Neon Purple
            PegType.MULTIBALL -> 0xFF00F5FF // Bright Electric Cyan
            PegType.LONG_AIM -> 0xFFFFEA00 // Golden Laser
            PegType.BLAST_RADIUS -> 0xFFFF1744 // Radiant Crimson
        }
        particles.add(
            Particle(
                x = peg.x,
                y = peg.y,
                vx = 0f,
                vy = 0f,
                color = ringColor,
                size = peg.width * 0.8f,
                life = 1f,
                maxLife = 1f,
                type = ParticleType.SHOCKWAVE_RING,
                growthRate = 95f + (comboCount * 3f).coerceAtMost(50f)
            )
        )

        // 2. High-speed directional and radial sparks
        val baseSparkCount = when {
            isOrange -> 20
            isGreen -> 22
            isPurple -> 18
            else -> 12 + (comboCount / 3).coerceAtMost(8)
        }

        val speedMultiplier = 1.0f + (comboCount * 0.04f).coerceAtMost(0.6f)

        for (i in 0 until baseSparkCount) {
            val ang = Random.nextFloat() * 2f * Math.PI.toFloat()
            val spd = (90f + Random.nextFloat() * 210f) * speedMultiplier
            val sparkColor = when {
                isOrange && i % 3 == 0 -> 0xFFFFD700 // Gold fire spark
                isGreen && i % 3 == 0 -> 0xFF76FF03  // Lime shimmer
                isPurple && i % 3 == 0 -> 0xFFFFFFFF // Diamond white
                else -> pegColor
            }

            particles.add(
                Particle(
                    x = impactX + (Random.nextFloat() - 0.5f) * 6f,
                    y = impactY + (Random.nextFloat() - 0.5f) * 6f,
                    vx = cos(ang) * spd,
                    vy = sin(ang) * spd,
                    color = sparkColor,
                    size = 3.5f + Random.nextFloat() * 3.5f,
                    life = 0.8f + Random.nextFloat() * 0.4f,
                    maxLife = 1.2f,
                    type = ParticleType.SPARK
                )
            )
        }

        // 3. Floating Embers for Orange (KOF flames) or Star Sparkles for Green/Purple
        if (isOrange) {
            for (i in 0 until 8) {
                val ang = Random.nextFloat() * 2f * Math.PI.toFloat()
                val spd = 30f + Random.nextFloat() * 80f
                particles.add(
                    Particle(
                        x = peg.x,
                        y = peg.y,
                        vx = cos(ang) * spd,
                        vy = sin(ang) * spd - 40f, // Upward flame drift
                        color = if (i % 2 == 0) 0xFFFF3D00 else 0xFFFFAB00,
                        size = 4f + Random.nextFloat() * 4f,
                        life = 1.0f + Random.nextFloat() * 0.5f,
                        maxLife = 1.5f,
                        type = ParticleType.GLOW_EMBER
                    )
                )
            }
        } else if (isGreen || isPurple) {
            val count = if (isGreen) 10 else 8
            for (i in 0 until count) {
                val ang = Random.nextFloat() * 2f * Math.PI.toFloat()
                val spd = 40f + Random.nextFloat() * 110f
                particles.add(
                    Particle(
                        x = peg.x,
                        y = peg.y,
                        vx = cos(ang) * spd,
                        vy = sin(ang) * spd,
                        color = if (isGreen) 0xFF69F0AE else 0xFFEA80FC,
                        size = 4.5f + Random.nextFloat() * 4f,
                        life = 0.9f + Random.nextFloat() * 0.4f,
                        maxLife = 1.3f,
                        type = ParticleType.STAR_SPARKLE,
                        rotation = Random.nextFloat() * 360f,
                        vRot = (Random.nextFloat() - 0.5f) * 360f
                    )
                )
            }
        }
    }

    private fun spawnBucketCatchCelebration(centerX: Float, centerY: Float) {
        // Golden shockwave ring
        particles.add(
            Particle(
                x = centerX,
                y = centerY,
                vx = 0f,
                vy = 0f,
                color = 0xFFFFD700,
                size = 18f,
                life = 1f,
                maxLife = 1f,
                type = ParticleType.SHOCKWAVE_RING,
                growthRate = 120f
            )
        )
        // Golden starburst sparkles
        for (i in 0 until 16) {
            val ang = Random.nextFloat() * 2f * Math.PI.toFloat()
            val spd = 70f + Random.nextFloat() * 180f
            particles.add(
                Particle(
                    x = centerX,
                    y = centerY,
                    vx = cos(ang) * spd,
                    vy = sin(ang) * spd - 60f,
                    color = if (i % 2 == 0) 0xFFFFD700 else 0xFFFFF176,
                    size = 5f + Random.nextFloat() * 4f,
                    life = 1.0f + Random.nextFloat() * 0.4f,
                    maxLife = 1.4f,
                    type = ParticleType.STAR_SPARKLE,
                    rotation = Random.nextFloat() * 360f,
                    vRot = (Random.nextFloat() - 0.5f) * 400f
                )
            )
        }
    }

    private fun spawnExplosionParticles(x: Float, y: Float, color: Long) {
        for (i in 0 until 14) {
            val ang = Random.nextFloat() * 2f * Math.PI.toFloat()
            val spd = 60f + Random.nextFloat() * 140f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(ang) * spd,
                    vy = sin(ang) * spd,
                    color = color,
                    size = 4f + Random.nextFloat() * 5f,
                    life = 1f,
                    maxLife = 1f
                )
            )
        }
    }

    private fun spawnFireworks(x: Float, y: Float) {
        val colors = listOf(0xFFFF3B30, 0xFFFF9500, 0xFFFFCC00, 0xFF34C759, 0xFF007AFF, 0xFFAF52DE)
        val color = colors.random()
        for (i in 0 until 24) {
            val ang = Random.nextFloat() * 2f * Math.PI.toFloat()
            val spd = 80f + Random.nextFloat() * 200f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(ang) * spd,
                    vy = sin(ang) * spd,
                    color = color,
                    size = 5f + Random.nextFloat() * 6f,
                    life = 1.2f,
                    maxLife = 1.2f
                )
            )
        }
    }

    private fun getPegColor(type: PegType): Long = when (type) {
        PegType.BLUE -> 0xFF4A90E2
        PegType.ORANGE -> 0xFFFF8A00
        PegType.GREEN -> 0xFF34C759
        PegType.PURPLE -> 0xFFBF5AF2
        PegType.MULTIBALL -> 0xFF00E5FF
        PegType.LONG_AIM -> 0xFFFFD700
        PegType.BLAST_RADIUS -> 0xFFFF2A2A
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
        audioEngine.stopFeverMelody()
        gameLoopJob?.cancel()
    }
}
