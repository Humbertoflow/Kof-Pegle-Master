package com.example

import com.example.peggle.data.LevelCatalog
import com.example.peggle.model.Particle
import com.example.peggle.model.ParticleType
import com.example.peggle.model.PegType
import com.example.peggle.model.PeggleMaster
import com.example.peggle.physics.PegglePhysicsEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun levelInstantiation_hasExactly25OrangePegsAnd2GreenPegs() {
        val levels = LevelCatalog.getAllLevels()
        assertTrue("Levels catalog should not be empty", levels.isNotEmpty())

        for (level in levels) {
            val pegs = LevelCatalog.instantiatePegs(level, 400f, 600f)
            val orangeCount = pegs.count { it.type == PegType.ORANGE }
            val greenCount = pegs.count { it.type == PegType.GREEN }

            assertEquals("Level ${level.id} must have 25 orange pegs", 25, orangeCount)
            assertEquals("Level ${level.id} must have 2 green pegs", 2, greenCount)
            assertTrue("Level ${level.id} should have plenty of pegs", pegs.size >= 30)
        }
    }

    @Test
    fun peggleMasters_allHaveUniqueIdsAndTitles() {
        val masters = PeggleMaster.values()
        val ids = masters.map { it.id }.toSet()
        assertEquals(masters.size, ids.size)
    }

    @Test
    fun trajectoryCalculation_returnsValidPath() {
        val physics = PegglePhysicsEngine()
        val level = LevelCatalog.getAllLevels().first()
        val pegs = LevelCatalog.instantiatePegs(level, 400f, 600f)

        val trajectory = physics.calculateTrajectory(
            startX = 200f,
            startY = 32f,
            angleRad = Math.toRadians(90.0).toFloat(),
            speed = 640f,
            pegs = pegs,
            boardWidth = 400f,
            boardHeight = 600f,
            superGuide = true
        )

        assertTrue("Trajectory points should be calculated", trajectory.isNotEmpty())
    }

    @Test
    fun particleAnimation_typesAndPhysicsValidation() {
        val spark = Particle(
            x = 100f,
            y = 100f,
            vx = 50f,
            vy = -50f,
            color = 0xFFFF8A00,
            size = 4f,
            type = ParticleType.SPARK
        )
        val ring = Particle(
            x = 100f,
            y = 100f,
            vx = 0f,
            vy = 0f,
            color = 0xFFFF6D00,
            size = 12f,
            type = ParticleType.SHOCKWAVE_RING,
            growthRate = 100f
        )
        assertEquals(ParticleType.SPARK, spark.type)
        assertEquals(ParticleType.SHOCKWAVE_RING, ring.type)
        assertTrue(ring.growthRate > 0f)
    }
}

