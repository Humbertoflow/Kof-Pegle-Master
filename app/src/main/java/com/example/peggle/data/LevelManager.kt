package com.example.peggle.data

import android.content.Context
import com.example.peggle.data.local.LevelConfigEntity
import com.example.peggle.data.local.LevelDao
import com.example.peggle.data.local.PegLayoutSerializer
import com.example.peggle.data.local.PeggleDatabase
import com.example.peggle.model.Peg
import com.example.peggle.model.PegType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * LevelManager manages local persistence and retrieval of Peggle level configurations,
 * including customizable peg layouts and count targets (orange pegs, green pegs,
 * purple pegs, initial balls, and par score) backed by Room database.
 */
class LevelManager(
    private val levelDao: LevelDao
) {

    constructor(context: Context) : this(
        PeggleDatabase.getInstance(context).levelDao()
    )

    /**
     * Observes all levels reactively from Room as LevelConfigEntity items.
     */
    fun observeAllLevels(): Flow<List<LevelConfigEntity>> = levelDao.getAllLevelsFlow()

    /**
     * Observes all levels reactively converted to LevelData models.
     */
    fun observeAllLevelData(): Flow<List<LevelData>> =
        levelDao.getAllLevelsFlow().map { entities ->
            entities.map { it.toLevelData() }
        }

    /**
     * Observes a specific level configuration reactively by ID.
     */
    fun observeLevelById(id: String): Flow<LevelConfigEntity?> =
        levelDao.observeLevelById(id)

    /**
     * Retrieves all level configurations from Room.
     */
    suspend fun getAllLevels(): List<LevelConfigEntity> = withContext(Dispatchers.IO) {
        levelDao.getAllLevels()
    }

    /**
     * Retrieves all levels converted to LevelData models.
     */
    suspend fun getAllLevelData(): List<LevelData> = withContext(Dispatchers.IO) {
        levelDao.getAllLevels().map { it.toLevelData() }
    }

    /**
     * Retrieves a single level configuration by ID.
     */
    suspend fun getLevelById(id: String): LevelConfigEntity? = withContext(Dispatchers.IO) {
        levelDao.getLevelById(id)
    }

    /**
     * Retrieves a single level converted to LevelData by ID.
     */
    suspend fun getLevelDataById(id: String): LevelData? = withContext(Dispatchers.IO) {
        levelDao.getLevelById(id)?.toLevelData()
    }

    /**
     * Ensures default levels from LevelCatalog are seeded into Room if the database is empty.
     */
    suspend fun ensureDefaultLevelsSeeded(forceReload: Boolean = false) = withContext(Dispatchers.IO) {
        val count = levelDao.getLevelCount()
        val allCatalogLevels = LevelCatalog.getAllLevels()
        if (count < allCatalogLevels.size || forceReload) {
            val defaultLevels = allCatalogLevels.map { level ->
                LevelConfigEntity.fromLevelData(
                    level = level,
                    targetOrangePegs = 25,
                    targetGreenPegs = 2,
                    targetPurplePegs = 1,
                    initialBallCount = 10,
                    parScore = 100_000 + (level.stageNumber * 5_000)
                )
            }
            levelDao.insertLevels(defaultLevels)
        }
    }

    /**
     * Stores or updates a level configuration in the Room database.
     */
    suspend fun saveLevel(levelConfig: LevelConfigEntity) = withContext(Dispatchers.IO) {
        levelDao.insertLevel(levelConfig)
    }

    /**
     * Stores a level from LevelData with specific count targets.
     */
    suspend fun saveLevel(
        levelData: LevelData,
        targetOrangePegs: Int = 25,
        targetGreenPegs: Int = 2,
        targetPurplePegs: Int = 1,
        initialBallCount: Int = 10,
        parScore: Int = 100_000
    ) = withContext(Dispatchers.IO) {
        val entity = LevelConfigEntity.fromLevelData(
            level = levelData,
            targetOrangePegs = targetOrangePegs,
            targetGreenPegs = targetGreenPegs,
            targetPurplePegs = targetPurplePegs,
            initialBallCount = initialBallCount,
            parScore = parScore
        )
        levelDao.insertLevel(entity)
    }

    /**
     * Updates target counts for an existing level.
     */
    suspend fun updateCountTargets(
        levelId: String,
        targetOrangePegs: Int,
        targetGreenPegs: Int = 2,
        targetPurplePegs: Int = 1,
        initialBallCount: Int = 10,
        parScore: Int = 100_000
    ): Boolean = withContext(Dispatchers.IO) {
        val existing = levelDao.getLevelById(levelId) ?: return@withContext false
        val updated = existing.copy(
            targetOrangePegs = targetOrangePegs,
            targetGreenPegs = targetGreenPegs,
            targetPurplePegs = targetPurplePegs,
            initialBallCount = initialBallCount,
            parScore = parScore
        )
        levelDao.updateLevel(updated)
        true
    }

    /**
     * Updates peg layout templates for an existing level.
     */
    suspend fun updatePegLayout(
        levelId: String,
        templates: List<PegTemplate>
    ): Boolean = withContext(Dispatchers.IO) {
        val existing = levelDao.getLevelById(levelId) ?: return@withContext false
        val updated = existing.copy(
            pegLayoutJson = PegLayoutSerializer.serialize(templates)
        )
        levelDao.updateLevel(updated)
        true
    }

    /**
     * Deletes a level by ID.
     */
    suspend fun deleteLevel(levelId: String) = withContext(Dispatchers.IO) {
        levelDao.deleteLevelById(levelId)
    }

    /**
     * Resets the database back to default catalog levels.
     */
    suspend fun resetToDefaultLevels() = withContext(Dispatchers.IO) {
        levelDao.deleteAllLevels()
        ensureDefaultLevelsSeeded(forceReload = true)
    }

    /**
     * Instantiates actual runtime Pegs based on a LevelConfigEntity,
     * applying the configured count targets (targetOrangePegs, targetGreenPegs)
     * and scaling the layout to the board dimensions.
     */
    fun instantiatePegs(
        config: LevelConfigEntity,
        boardWidth: Float,
        boardHeight: Float,
        densityMultiplier: Float = 1.0f
    ): List<Peg> {
        val templates = config.getPegTemplates()
        return instantiatePegsFromTemplates(
            templates = templates,
            boardWidth = boardWidth,
            boardHeight = boardHeight,
            targetOrange = config.targetOrangePegs,
            targetGreen = config.targetGreenPegs,
            densityMultiplier = densityMultiplier
        )
    }

    /**
     * Instantiates actual runtime Pegs based on LevelData and target counts.
     */
    fun instantiatePegs(
        level: LevelData,
        boardWidth: Float,
        boardHeight: Float,
        targetOrange: Int = 25,
        targetGreen: Int = 2,
        densityMultiplier: Float = 1.0f
    ): List<Peg> {
        return instantiatePegsFromTemplates(
            templates = level.templates,
            boardWidth = boardWidth,
            boardHeight = boardHeight,
            targetOrange = targetOrange,
            targetGreen = targetGreen,
            densityMultiplier = densityMultiplier
        )
    }

    private fun instantiatePegsFromTemplates(
        templates: List<PegTemplate>,
        boardWidth: Float,
        boardHeight: Float,
        targetOrange: Int,
        targetGreen: Int,
        densityMultiplier: Float = 1.0f
    ): List<Peg> {
        val filteredTemplates = when {
            // Less density for assisted mode: reduce background clutter/blue pegs while keeping enough pegs for goals
            densityMultiplier < 0.95f -> {
                val neededCount = (targetOrange + targetGreen + 6).coerceAtMost(templates.size)
                val targetCount = (templates.size * densityMultiplier).toInt().coerceAtLeast(neededCount)
                if (targetCount < templates.size) {
                    // Evenly thin out templates while preserving overall silhouette
                    val step = templates.size.toFloat() / targetCount.toFloat()
                    (0 until targetCount).map { i -> templates[(i * step).toInt().coerceAtMost(templates.size - 1)] }
                } else {
                    templates
                }
            }
            // More density for challenge mode: inject interstitial decorative obstacles
            densityMultiplier > 1.05f -> {
                val additional = mutableListOf<PegTemplate>()
                val extraCount = ((densityMultiplier - 1.0f) * templates.size).toInt().coerceAtMost(25)
                for (i in 0 until extraCount) {
                    val base = templates[(i * 3) % templates.size]
                    // Offset interstitial peg slightly
                    val offsetX = if (i % 2 == 0) 0.035f else -0.035f
                    val offsetY = if (i % 4 == 0) 0.03f else -0.03f
                    val nx = (base.xRatio + offsetX).coerceIn(0.12f, 0.88f)
                    val ny = (base.yRatio + offsetY).coerceIn(0.20f, 0.82f)
                    additional.add(
                        PegTemplate(
                            xRatio = nx,
                            yRatio = ny,
                            shape = base.shape,
                            width = base.width * 0.9f,
                            height = base.height * 0.9f,
                            rotation = base.rotation
                        )
                    )
                }
                templates + additional
            }
            else -> templates
        }

        val pegs = mutableListOf<Peg>()
        val allIndices = filteredTemplates.indices.shuffled()
        val orangeIndices = mutableSetOf<Int>()
        val greenIndices = mutableSetOf<Int>()

        val safeTargetOrange = targetOrange.coerceAtMost((filteredTemplates.size - 3).coerceAtLeast(1))
        for (i in 0 until safeTargetOrange) {
            if (i < allIndices.size) {
                orangeIndices.add(allIndices[i])
            }
        }

        val safeTargetGreen = targetGreen.coerceAtMost(filteredTemplates.size - safeTargetOrange)
        for (i in safeTargetOrange until safeTargetOrange + safeTargetGreen) {
            if (i < allIndices.size) {
                greenIndices.add(allIndices[i])
            }
        }

        // Special Tactical Power-Up Pegs: Multiball, Long Aim, Blast Radius
        val multiballIndices = mutableSetOf<Int>()
        val longAimIndices = mutableSetOf<Int>()
        val blastRadiusIndices = mutableSetOf<Int>()

        var nextIndex = safeTargetOrange + safeTargetGreen
        if (nextIndex < allIndices.size) {
            multiballIndices.add(allIndices[nextIndex++])
        }
        if (nextIndex < allIndices.size) {
            longAimIndices.add(allIndices[nextIndex++])
        }
        if (nextIndex < allIndices.size) {
            blastRadiusIndices.add(allIndices[nextIndex++])
        }

        var pegId = 0
        filteredTemplates.forEachIndexed { index, t ->
            val type = when {
                orangeIndices.contains(index) -> PegType.ORANGE
                greenIndices.contains(index) -> PegType.GREEN
                multiballIndices.contains(index) -> PegType.MULTIBALL
                longAimIndices.contains(index) -> PegType.LONG_AIM
                blastRadiusIndices.contains(index) -> PegType.BLAST_RADIUS
                t.defaultType.isSpecialPowerUp -> t.defaultType
                else -> PegType.BLUE
            }

            pegs.add(
                Peg(
                    id = pegId++,
                    x = t.xRatio * boardWidth,
                    y = t.yRatio * boardHeight,
                    type = type,
                    shape = t.shape,
                    width = t.width,
                    height = t.height,
                    rotation = t.rotation
                )
            )
        }

        return pegs
    }

    companion object {
        @Volatile
        private var INSTANCE: LevelManager? = null

        fun getInstance(context: Context): LevelManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LevelManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
