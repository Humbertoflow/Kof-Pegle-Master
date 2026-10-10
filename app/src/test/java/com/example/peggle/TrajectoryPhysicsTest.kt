package com.example.peggle

import com.example.peggle.model.Ball
import com.example.peggle.model.Bucket
import com.example.peggle.model.Peg
import com.example.peggle.model.PegShape
import com.example.peggle.model.PegType
import com.example.peggle.physics.PegglePhysicsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.PI

class TrajectoryPhysicsTest {

    private lateinit var physicsEngine: PegglePhysicsEngine

    @Before
    fun setUp() {
        physicsEngine = PegglePhysicsEngine()
    }

    @Test
    fun calculateTrajectory_producesSmoothPathPoints() {
        val startX = 200f
        val startY = 32f
        val angleRad = (PI / 2.0).toFloat() // Straight down (90 degrees)
        val speed = 640f
        val boardWidth = 400f
        val boardHeight = 800f

        val points = physicsEngine.calculateTrajectory(
            startX = startX,
            startY = startY,
            angleRad = angleRad,
            speed = speed,
            pegs = emptyList(),
            boardWidth = boardWidth,
            boardHeight = boardHeight,
            superGuide = false
        )

        assertTrue("Trajectory should produce at least 10 points", points.size >= 10)
        assertEquals(startX, points.first().x, 0.001f)
        assertEquals(startY, points.first().y, 0.001f)
        assertFalse(points.first().isBounce)
    }

    @Test
    fun calculateTrajectory_detectsWallBounce() {
        val startX = 50f
        val startY = 32f
        // Aiming steeply left towards the left wall (160 degrees)
        val angleRad = Math.toRadians(160.0).toFloat()
        val speed = 640f
        val boardWidth = 400f
        val boardHeight = 800f

        val points = physicsEngine.calculateTrajectory(
            startX = startX,
            startY = startY,
            angleRad = angleRad,
            speed = speed,
            pegs = emptyList(),
            boardWidth = boardWidth,
            boardHeight = boardHeight,
            superGuide = false
        )

        val wallBounce = points.firstOrNull { it.isBounce && it.bounceType == PegglePhysicsEngine.BounceType.WALL }
        assertTrue("Trajectory must predict wall bounce", wallBounce != null)
        assertEquals(11f, wallBounce!!.x, 0.5f) // Ball radius is 11f
        assertTrue("Wall bounce normal should point inward (+X)", wallBounce.bounceNormalX > 0)
    }

    @Test
    fun calculateTrajectory_detectsCircularPegBounce() {
        val startX = 200f
        val startY = 32f
        val angleRad = (PI / 2.0).toFloat() // Straight down
        val speed = 640f
        val boardWidth = 400f
        val boardHeight = 800f

        // Place a peg right along the vertical path
        val targetPeg = Peg(
            id = 42,
            x = 200f,
            y = 250f,
            type = PegType.ORANGE,
            shape = PegShape.CIRCLE,
            width = 14f,
            height = 14f
        )

        val points = physicsEngine.calculateTrajectory(
            startX = startX,
            startY = startY,
            angleRad = angleRad,
            speed = speed,
            pegs = listOf(targetPeg),
            boardWidth = boardWidth,
            boardHeight = boardHeight,
            superGuide = false
        )

        val pegBounce = points.firstOrNull { it.isBounce && it.hitPegId == 42 }
        assertTrue("Trajectory must predict peg bounce", pegBounce != null)
        assertEquals(PegglePhysicsEngine.BounceType.PEG_CIRCLE, pegBounce!!.bounceType)
        assertEquals(42, pegBounce.hitPegId)
        assertTrue("Bounce point should be above or on top of peg", pegBounce.y < 250f)
    }

    @Test
    fun calculateTrajectory_detectsRotatedBrickPegBounce() {
        val startX = 200f
        val startY = 32f
        val angleRad = (PI / 2.0).toFloat() // Straight down
        val speed = 640f
        val boardWidth = 400f
        val boardHeight = 800f

        // Place a tilted brick peg along the path
        val brickPeg = Peg(
            id = 99,
            x = 200f,
            y = 300f,
            type = PegType.BLUE,
            shape = PegShape.BRICK,
            width = 40f,
            height = 14f,
            rotation = 25f
        )

        val points = physicsEngine.calculateTrajectory(
            startX = startX,
            startY = startY,
            angleRad = angleRad,
            speed = speed,
            pegs = listOf(brickPeg),
            boardWidth = boardWidth,
            boardHeight = boardHeight,
            superGuide = false
        )

        val brickBounce = points.firstOrNull { it.isBounce && it.hitPegId == 99 }
        assertTrue("Trajectory must predict brick peg bounce", brickBounce != null)
        assertEquals(PegglePhysicsEngine.BounceType.PEG_BRICK, brickBounce!!.bounceType)
        assertEquals(99, brickBounce.hitPegId)
    }

    @Test
    fun calculateTrajectory_superGuideExtendsPredictionLengthAndBounces() {
        val startX = 50f
        val startY = 32f
        val angleRad = Math.toRadians(150.0).toFloat()
        val speed = 640f
        val boardWidth = 400f
        val boardHeight = 800f

        val normalPoints = physicsEngine.calculateTrajectory(
            startX = startX,
            startY = startY,
            angleRad = angleRad,
            speed = speed,
            pegs = emptyList(),
            boardWidth = boardWidth,
            boardHeight = boardHeight,
            superGuide = false
        )

        val superPoints = physicsEngine.calculateTrajectory(
            startX = startX,
            startY = startY,
            angleRad = angleRad,
            speed = speed,
            pegs = emptyList(),
            boardWidth = boardWidth,
            boardHeight = boardHeight,
            superGuide = true
        )

        assertTrue(
            "Super Guide should produce more trajectory prediction points",
            superPoints.size > normalPoints.size
        )
    }

    @Test
    fun update_appliesGravitationalAcceleration() {
        val ball = Ball(x = 200f, y = 100f, vx = 0f, vy = 0f, active = true)
        val bucket = Bucket(x = 200f, y = 700f)
        val dt = 0.1f

        physicsEngine.update(
            ball = ball,
            pegs = emptyList(),
            bucket = bucket,
            boardWidth = 400f,
            boardHeight = 800f,
            dt = dt,
            onPegHit = {},
            onRimBounce = {}
        )

        // vy should increase by approximately gravity * dt (680 * 0.1 = 68)
        assertTrue("Ball downward velocity vy should accelerate due to gravity", ball.vy > 60f)
        assertTrue("Ball y position should increase downward", ball.y > 100f)
    }

    @Test
    fun update_detectsPegCollisionAndDeflects() {
        // Place a ball slightly above a peg moving downward
        val ball = Ball(x = 200f, y = 230f, vx = 0f, vy = 200f, active = true)
        val targetPeg = Peg(
            id = 10,
            x = 200f,
            y = 250f,
            type = PegType.ORANGE,
            shape = PegShape.CIRCLE,
            width = 14f,
            height = 14f
        )
        val bucket = Bucket(x = 200f, y = 700f)
        var pegHitRecorded = false

        physicsEngine.update(
            ball = ball,
            pegs = listOf(targetPeg),
            bucket = bucket,
            boardWidth = 400f,
            boardHeight = 800f,
            dt = 0.05f,
            onPegHit = { pegHitRecorded = true },
            onRimBounce = {}
        )

        assertTrue("onPegHit callback must be triggered on contact", pegHitRecorded)
    }

    @Test
    fun update_catchesBallInBucket() {
        val bucket = Bucket(x = 200f, y = 600f, width = 64f, height = 24f)
        // Ball right at top opening of bucket falling downward
        val ball = Ball(x = 200f, y = 588f, vx = 0f, vy = 150f, active = true)

        val caught = physicsEngine.update(
            ball = ball,
            pegs = emptyList(),
            bucket = bucket,
            boardWidth = 400f,
            boardHeight = 800f,
            dt = 0.02f,
            onPegHit = {},
            onRimBounce = {}
        )

        assertTrue("Ball falling into bucket top opening must be caught", caught)
        assertFalse("Caught ball must be deactivated", ball.active)
    }
}
