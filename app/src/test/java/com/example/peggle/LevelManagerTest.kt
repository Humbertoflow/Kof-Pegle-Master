package com.example.peggle

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.peggle.data.LevelCatalog
import com.example.peggle.data.LevelManager
import com.example.peggle.data.PegTemplate
import com.example.peggle.data.local.LevelConfigEntity
import com.example.peggle.data.local.PeggleDatabase
import com.example.peggle.model.PegShape
import com.example.peggle.model.PegType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LevelManagerTest {

    private lateinit var database: PeggleDatabase
    private lateinit var levelManager: LevelManager

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PeggleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        levelManager = LevelManager(database.levelDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun ensureDefaultLevelsSeeded_populatesRoomDatabase() = runBlocking {
        levelManager.ensureDefaultLevelsSeeded()
        val levels = levelManager.getAllLevels()

        assertEquals(208, levels.size)
        val level1 = levels.first()
        assertEquals("level_1", level1.id)
        assertEquals(25, level1.targetOrangePegs)
        assertEquals(2, level1.targetGreenPegs)
        assertEquals(10, level1.initialBallCount)
        assertTrue(level1.getPegTemplates().isNotEmpty())

        val lastLevel = levels.last()
        assertEquals("level_208", lastLevel.id)
        assertEquals(208, lastLevel.stageNumber)
    }

    @Test
    fun updateCountTargets_modifiesTargetsCorrectly() = runBlocking {
        levelManager.ensureDefaultLevelsSeeded()

        val updated = levelManager.updateCountTargets(
            levelId = "level_1",
            targetOrangePegs = 15,
            targetGreenPegs = 3,
            targetPurplePegs = 2,
            initialBallCount = 12,
            parScore = 150_000
        )
        assertTrue(updated)

        val retrieved = levelManager.getLevelById("level_1")
        assertNotNull(retrieved)
        assertEquals(15, retrieved!!.targetOrangePegs)
        assertEquals(3, retrieved.targetGreenPegs)
        assertEquals(2, retrieved.targetPurplePegs)
        assertEquals(12, retrieved.initialBallCount)
        assertEquals(150_000, retrieved.parScore)
    }

    @Test
    fun updatePegLayout_storesAndRetrievesTemplates() = runBlocking {
        levelManager.ensureDefaultLevelsSeeded()

        val customTemplates = listOf(
            PegTemplate(xRatio = 0.2f, yRatio = 0.3f, defaultType = PegType.BLUE, shape = PegShape.CIRCLE, width = 16f, height = 16f),
            PegTemplate(xRatio = 0.5f, yRatio = 0.5f, defaultType = PegType.ORANGE, shape = PegShape.BRICK, width = 30f, height = 12f, rotation = 45f),
            PegTemplate(xRatio = 0.8f, yRatio = 0.7f, defaultType = PegType.GREEN, shape = PegShape.CIRCLE, width = 14f, height = 14f)
        )

        val updated = levelManager.updatePegLayout("level_2", customTemplates)
        assertTrue(updated)

        val retrieved = levelManager.getLevelById("level_2")
        assertNotNull(retrieved)
        val templates = retrieved!!.getPegTemplates()
        assertEquals(3, templates.size)
        assertEquals(0.2f, templates[0].xRatio, 0.001f)
        assertEquals(PegShape.BRICK, templates[1].shape)
        assertEquals(45f, templates[1].rotation, 0.001f)
    }

    @Test
    fun instantiatePegs_respectsCustomCountTargets() = runBlocking {
        levelManager.ensureDefaultLevelsSeeded()

        // Configure custom target: 10 orange pegs and 3 green pegs
        levelManager.updateCountTargets(
            levelId = "level_1",
            targetOrangePegs = 10,
            targetGreenPegs = 3
        )

        val config = levelManager.getLevelById("level_1")!!
        val pegs = levelManager.instantiatePegs(config, boardWidth = 400f, boardHeight = 600f)

        val orangeCount = pegs.count { it.type == PegType.ORANGE }
        val greenCount = pegs.count { it.type == PegType.GREEN }

        assertEquals(10, orangeCount)
        assertEquals(3, greenCount)
        assertTrue(pegs.size >= 30)
    }

    @Test
    fun saveLevel_customLevelConfig_persistedAndRetrieved() = runBlocking {
        val customLevel = LevelConfigEntity(
            id = "custom_test_level",
            stageNumber = 99,
            title = "Test Dojo",
            subtitle = "Custom created level",
            masterName = "BJORN",
            bgGradientStart = 0xFF000000,
            bgGradientEnd = 0xFF111111,
            bgDrawableRes = com.example.R.drawable.bg_kof_kyo,
            targetOrangePegs = 18,
            targetGreenPegs = 4,
            targetPurplePegs = 1,
            initialBallCount = 8,
            parScore = 80_000,
            pegLayoutJson = "[]"
        )

        levelManager.saveLevel(customLevel)

        val retrieved = levelManager.getLevelById("custom_test_level")
        assertNotNull(retrieved)
        assertEquals("Test Dojo", retrieved!!.title)
        assertEquals(18, retrieved.targetOrangePegs)
        assertEquals(8, retrieved.initialBallCount)
    }
}
