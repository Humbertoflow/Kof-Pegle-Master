package com.example.peggle.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peggle.data.LevelCatalog
import com.example.peggle.model.GameMode
import com.example.peggle.model.ShotPhase
import com.example.peggle.ui.components.AimControlPanel
import com.example.peggle.ui.components.ExtremeFeverOverlay
import com.example.peggle.ui.components.ExtremeFeverInGameBanner
import com.example.peggle.ui.components.FeverMeter
import com.example.peggle.ui.components.GameOverDialog
import com.example.peggle.ui.components.PauseDialog
import com.example.peggle.ui.components.PeggleBoardCanvas
import com.example.peggle.ui.components.ScoreHeader
import com.example.peggle.viewmodel.PeggleViewModel
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonYellow

@Composable
fun GamePlayScreen(
    viewModel: PeggleViewModel,
    onNavigateHome: () -> Unit,
    onNavigateToMap: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val density = LocalDensity.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val toggleOrientation = {
        (context as? Activity)?.let { act ->
            act.requestedOrientation = if (isLandscape) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF04040A))
    ) {
        if (isLandscape) {
            // ==========================================
            // HORIZONTAL / LANDSCAPE WIDESCREEN LAYOUT
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // LEFT COLUMN (Fever Meter, Master Avatar, Balls, Controls)
                Column(
                    modifier = Modifier
                        .width(170.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF070714).copy(alpha = 0.95f))
                        .border(
                            width = 1.dp,
                            color = NeonCyan.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                        )
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Info
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.levelData.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(uiState.selectedMaster.accentColor))
                                    .border(1.5.dp, NeonCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = uiState.selectedMaster.emoji, fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.selectedMaster.title,
                                color = NeonCyan,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Vertical Fever Meter
                    FeverMeter(
                        orangePegsRemaining = uiState.orangePegsRemaining,
                        totalOrangePegs = uiState.totalOrangePegs,
                        currentMultiplier = viewModel.getFeverMultiplier(),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Balls left row
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ESFERAS: ${uiState.ballsRemaining}",
                            color = NeonYellow,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val count = uiState.ballsRemaining.coerceAtMost(8)
                            for (i in 0 until count) {
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .size(11.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(Color.White, Color(0xFF00E5FF), Color(0xFF003B5C))
                                            )
                                        )
                                        .border(1.dp, NeonCyan, CircleShape)
                                )
                            }
                        }
                    }

                    // Action buttons (Map, Rotate, Sound, Pause)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onNavigateToMap != null) {
                            IconButton(
                                onClick = onNavigateToMap,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("landscape_map_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Mapa",
                                    tint = NeonYellow,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { toggleOrientation() },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("landscape_rotate_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenRotation,
                                contentDescription = "Rotar",
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleSound() },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("landscape_sound_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                contentDescription = "Sonido",
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.togglePause() },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("landscape_pause_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pausa",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // CENTER COLUMN (Main 3D Peggle Board)
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    val centerW = with(density) { maxWidth.toPx() }
                    val centerH = with(density) { maxHeight.toPx() }

                    LaunchedEffect(centerW, centerH) {
                        viewModel.setBoardDimensions(centerW, centerH)
                    }

                    PeggleBoardCanvas(
                        pegs = viewModel.pegs,
                        ball = viewModel.ball,
                        bucket = viewModel.bucket,
                        scoreBins = viewModel.scoreBins,
                        scoreFloaters = viewModel.scoreFloaters,
                        particles = viewModel.particles,
                        trajectoryPoints = viewModel.trajectoryPoints,
                        cannonAngleDeg = uiState.cannonAngleDeg,
                        shotPhase = uiState.shotPhase,
                        backgroundGradient = uiState.levelData.backgroundGradient,
                        bgDrawableRes = uiState.levelData.bgDrawableRes,
                        extraBalls = viewModel.extraBalls,
                        onAimChange = { viewModel.setCannonAngle(it) },
                        onFireTap = { viewModel.fireBall() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // RIGHT COLUMN (HUD Score & Aim Cockpit Controls)
                Column(
                    modifier = Modifier
                        .width(170.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF070714).copy(alpha = 0.95f))
                        .border(
                            width = 1.dp,
                            color = NeonCyan.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                        )
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Score HUD
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0E1026))
                            .border(1.dp, Color(0xFF1E2442), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "PUNTUACIÓN",
                            color = Color(0xFF8B9CB5),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                        Text(
                            text = String.format("%,d", uiState.currentScore),
                            color = NeonYellow,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "RÉCORD: ${String.format("%,d", uiState.highscore)}",
                            color = NeonCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Duel Banner if Duel Mode
                    if (uiState.gameMode == GameMode.DUEL) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0E1026))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "J1: ${String.format("%,d", uiState.duelPlayer1Score)}",
                                color = if (uiState.duelCurrentTurnPlayer == 1) NeonCyan else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "J2: ${String.format("%,d", uiState.duelPlayer2Score)}",
                                color = if (uiState.duelCurrentTurnPlayer == 2) NeonMagenta else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Precision Aim Controls
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ÁNGULO: ${uiState.cannonAngleDeg.toInt()}°",
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )

                        Slider(
                            value = uiState.cannonAngleDeg,
                            onValueChange = { viewModel.setCannonAngle(it) },
                            valueRange = 18f..162f,
                            enabled = uiState.shotPhase == ShotPhase.AIMING,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("landscape_aim_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan,
                                inactiveTrackColor = Color(0xFF1E2442)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FilledIconButton(
                                onClick = { viewModel.nudgeAngle(-1.5f) },
                                enabled = uiState.shotPhase == ShotPhase.AIMING,
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("landscape_nudge_left"),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Color(0xFF101326),
                                    contentColor = NeonCyan
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "-1.5°"
                                )
                            }

                            FilledIconButton(
                                onClick = { viewModel.nudgeAngle(1.5f) },
                                enabled = uiState.shotPhase == ShotPhase.AIMING,
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("landscape_nudge_right"),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Color(0xFF101326),
                                    contentColor = NeonCyan
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "+1.5°"
                                )
                            }
                        }
                    }

                    // Fast forward or Power message
                    if (uiState.shotPhase == ShotPhase.BALL_IN_PLAY) {
                        FilledIconButton(
                            onClick = { viewModel.toggleFastForward() },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("landscape_fast_forward"),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (uiState.isFastForward) NeonOrange else Color(0xFF101326),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "2X"
                            )
                        }
                    }

                    // Big Neon FIRE Button
                    Button(
                        onClick = { viewModel.fireBall() },
                        enabled = uiState.shotPhase == ShotPhase.AIMING && uiState.ballsRemaining > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("landscape_fire_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonOrange,
                            disabledContainerColor = Color(0xFF262A3E)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "¡FUEGO!",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // VERTICAL / PORTRAIT HANDHELD LAYOUT
            // ==========================================
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val widthPx = with(density) { maxWidth.toPx() }
                val heightPx = with(density) { maxHeight.toPx() }

                LaunchedEffect(widthPx, heightPx) {
                    viewModel.setBoardDimensions(widthPx, heightPx)
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // Main Interactive Peggle Canvas
                    PeggleBoardCanvas(
                        pegs = viewModel.pegs,
                        ball = viewModel.ball,
                        bucket = viewModel.bucket,
                        scoreBins = viewModel.scoreBins,
                        scoreFloaters = viewModel.scoreFloaters,
                        particles = viewModel.particles,
                        trajectoryPoints = viewModel.trajectoryPoints,
                        cannonAngleDeg = uiState.cannonAngleDeg,
                        shotPhase = uiState.shotPhase,
                        backgroundGradient = uiState.levelData.backgroundGradient,
                        bgDrawableRes = uiState.levelData.bgDrawableRes,
                        extraBalls = viewModel.extraBalls,
                        onAimChange = { viewModel.setCannonAngle(it) },
                        onFireTap = { viewModel.fireBall() },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Top Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    ) {
                        ScoreHeader(
                            title = "Nivel ${uiState.levelData.stageNumber}: ${uiState.levelData.title}",
                            score = uiState.currentScore,
                            highScore = uiState.highscore,
                            ballsRemaining = uiState.ballsRemaining,
                            master = uiState.selectedMaster,
                            soundEnabled = uiState.soundEnabled,
                            powerMessage = uiState.lastSpecialPowerMessage,
                            onToggleSound = { viewModel.toggleSound() },
                            onPause = { viewModel.togglePause() },
                            onToggleOrientation = { toggleOrientation() },
                            onOpenMap = onNavigateToMap
                        )

                        // Duel Mode Banner
                        if (uiState.gameMode == GameMode.DUEL) {
                            DuelScoreBanner(
                                p1Score = uiState.duelPlayer1Score,
                                p2Score = uiState.duelPlayer2Score,
                                currentTurn = uiState.duelCurrentTurnPlayer
                            )
                        }
                    }

                    // Left Side Fever Thermometer Meter
                    FeverMeter(
                        orangePegsRemaining = uiState.orangePegsRemaining,
                        totalOrangePegs = uiState.totalOrangePegs,
                        currentMultiplier = viewModel.getFeverMultiplier(),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp)
                    )

                    // Bottom Aim and Fire Control Panel
                    AimControlPanel(
                        cannonAngleDeg = uiState.cannonAngleDeg,
                        ballsRemaining = uiState.ballsRemaining,
                        shotPhase = uiState.shotPhase,
                        isFastForward = uiState.isFastForward,
                        onAngleChange = { viewModel.setCannonAngle(it) },
                        onNudgeAngle = { viewModel.nudgeAngle(it) },
                        onFire = { viewModel.fireBall() },
                        onToggleFastForward = { viewModel.toggleFastForward() },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }

        // Extreme Fever In-Game Celebration Banner when orange pegs are cleared
        if (uiState.shotPhase == ShotPhase.EXTREME_FEVER && !uiState.showVictoryDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                ExtremeFeverInGameBanner()
            }
        }

        // Extreme Fever Victory Celebration Dialog
        if (uiState.showVictoryDialog) {
            ExtremeFeverOverlay(
                score = uiState.currentScore,
                stars = uiState.earnedStars,
                isMasterClear = uiState.isMasterClear,
                feverBinScore = uiState.feverBinScoreAwarded,
                ballsLeftBonus = uiState.ballsRemaining * 10000,
                onNextLevel = {
                    val allLevels = LevelCatalog.getAllLevels()
                    val currentIndex = allLevels.indexOfFirst { it.id == uiState.levelData.id }
                    val nextLevel = allLevels.getOrElse((currentIndex + 1) % allLevels.size) { allLevels.first() }
                    viewModel.loadLevel(nextLevel, nextLevel.master)
                },
                onRestart = { viewModel.restartCurrentLevel() },
                onHome = onNavigateHome,
                onOpenMap = onNavigateToMap
            )
        }

        // Game Over Dialog
        if (uiState.showGameOverDialog) {
            GameOverDialog(
                orangeRemaining = uiState.orangePegsRemaining,
                score = uiState.currentScore,
                onRestart = { viewModel.restartCurrentLevel() },
                onHome = onNavigateHome,
                onOpenMap = onNavigateToMap
            )
        }

        // Pause Dialog
        if (uiState.isPaused) {
            PauseDialog(
                soundEnabled = uiState.soundEnabled,
                hapticsEnabled = uiState.hapticsEnabled,
                onResume = { viewModel.togglePause() },
                onRestart = {
                    viewModel.togglePause()
                    viewModel.restartCurrentLevel()
                },
                onToggleSound = { viewModel.toggleSound() },
                onToggleHaptics = { viewModel.toggleHaptics() },
                onToggleOrientation = { toggleOrientation() },
                onHome = {
                    viewModel.togglePause()
                    onNavigateHome()
                },
                onOpenMap = if (onNavigateToMap != null) {
                    {
                        viewModel.togglePause()
                        onNavigateToMap()
                    }
                } else null
            )
        }
    }
}

@Composable
private fun DuelScoreBanner(p1Score: Int, p2Score: Int, currentTurn: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player 1 (Neon Cyan)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (currentTurn == 1) Color(0xFF003B5C) else Color(0xFF0A0D1A))
                .border(
                    width = if (currentTurn == 1) 1.5.dp else 1.dp,
                    color = if (currentTurn == 1) Color(0xFF00F0FF) else Color(0xFF1E2442),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Column {
                Text(
                    text = "Jugador 1 ${if (currentTurn == 1) "⚡" else ""}",
                    color = Color(0xFF00F0FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    text = "${String.format("%,d", p1Score)} pts",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }

        Text(
            text = "VS",
            color = Color(0xFFFFEA00),
            fontWeight = FontWeight.Black,
            fontSize = 14.sp
        )

        // Player 2 (Neon Magenta)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (currentTurn == 2) Color(0xFF5C0038) else Color(0xFF0A0D1A))
                .border(
                    width = if (currentTurn == 2) 1.5.dp else 1.dp,
                    color = if (currentTurn == 2) Color(0xFFFF007F) else Color(0xFF1E2442),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (currentTurn == 2) "⚡ " else ""}Jugador 2",
                    color = Color(0xFFFF007F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    text = "${String.format("%,d", p2Score)} pts",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }
    }
}
