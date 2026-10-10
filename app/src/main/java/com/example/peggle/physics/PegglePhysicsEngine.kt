package com.example.peggle.physics

import com.example.peggle.model.Ball
import com.example.peggle.model.Bucket
import com.example.peggle.model.Peg
import com.example.peggle.model.PegShape
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PegglePhysicsEngine {

    var gravity: Float = 680f
    private val restitution: Float = 0.78f

    enum class BounceType {
        NONE,
        WALL,
        PEG_CIRCLE,
        PEG_BRICK
    }

    data class TrajectoryPoint(
        val x: Float,
        val y: Float,
        val isBounce: Boolean = false,
        val bounceType: BounceType = BounceType.NONE,
        val hitPegId: Int = -1,
        val bounceNormalX: Float = 0f,
        val bounceNormalY: Float = 0f
    )

    /**
     * Advances the ball physics by deltaTime using sub-stepping.
     * Returns true if ball was caught in the bucket, or false otherwise.
     */
    fun update(
        ball: Ball,
        pegs: List<Peg>,
        bucket: Bucket,
        boardWidth: Float,
        boardHeight: Float,
        dt: Float,
        onPegHit: (Peg) -> Unit,
        onRimBounce: () -> Unit
    ): Boolean {
        if (!ball.active) return false

        val subSteps = 4
        val subDt = dt / subSteps

        for (step in 0 until subSteps) {
            // Apply gravity
            ball.vy += gravity * subDt

            // Integrate position
            ball.x += ball.vx * subDt
            ball.y += ball.vy * subDt

            // Record trail
            if (step == 0) {
                ball.trail.add(Pair(ball.x, ball.y))
                if (ball.trail.size > 14) {
                    ball.trail.removeAt(0)
                }
            }

            // Wall collisions
            if (ball.x - ball.radius < 0f) {
                ball.x = ball.radius
                ball.vx = -ball.vx * restitution
            } else if (ball.x + ball.radius > boardWidth) {
                ball.x = boardWidth - ball.radius
                ball.vx = -ball.vx * restitution
            }

            if (ball.y - ball.radius < 0f) {
                ball.y = ball.radius
                ball.vy = -ball.vy * restitution
            }

            // Peg collisions
            for (peg in pegs) {
                if (peg.isRemoved) continue

                if (peg.shape == PegShape.CIRCLE) {
                    val dx = ball.x - peg.x
                    val dy = ball.y - peg.y
                    val distSq = dx * dx + dy * dy
                    val minDist = ball.radius + peg.radius

                    if (distSq < minDist * minDist) {
                        val dist = sqrt(distSq).coerceAtLeast(0.001f)
                        val nx = dx / dist
                        val ny = dy / dist

                        // Resolve penetration
                        val overlap = minDist - dist
                        ball.x += nx * overlap
                        ball.y += ny * overlap

                        // Reflect velocity
                        val dot = ball.vx * nx + ball.vy * ny
                        if (dot < 0f) {
                            ball.vx -= (1f + restitution) * dot * nx
                            ball.vy -= (1f + restitution) * dot * ny

                            // Add subtle Pachinko spin perturbation
                            val tangentX = -ny
                            val tangentY = nx
                            ball.vx += tangentX * 15f
                            ball.vy += tangentY * 15f
                        }

                        onPegHit(peg)
                    }
                } else {
                    // Brick collision (rotated rectangle)
                    val rad = Math.toRadians(-peg.rotation.toDouble()).toFloat()
                    val cosA = cos(rad)
                    val sinA = sin(rad)

                    val relX = ball.x - peg.x
                    val relY = ball.y - peg.y

                    val localX = relX * cosA - relY * sinA
                    val localY = relX * sinA + relY * cosA

                    val halfW = peg.width / 2f
                    val halfH = peg.height / 2f

                    val clampedX = localX.coerceIn(-halfW, halfW)
                    val clampedY = localY.coerceIn(-halfH, halfH)

                    val diffX = localX - clampedX
                    val diffY = localY - clampedY
                    val distSq = diffX * diffX + diffY * diffY

                    if (distSq < ball.radius * ball.radius) {
                        val dist = sqrt(distSq).coerceAtLeast(0.001f)
                        val localNx = diffX / dist
                        val localNy = diffY / dist

                        // Transform normal back to world
                        val worldRad = Math.toRadians(peg.rotation.toDouble()).toFloat()
                        val wCos = cos(worldRad)
                        val wSin = sin(worldRad)

                        val nx = localNx * wCos - localNy * wSin
                        val ny = localNx * wSin + localNy * wCos

                        val overlap = ball.radius - dist
                        ball.x += nx * overlap
                        ball.y += ny * overlap

                        val dot = ball.vx * nx + ball.vy * ny
                        if (dot < 0f) {
                            ball.vx -= (1f + restitution) * dot * nx
                            ball.vy -= (1f + restitution) * dot * ny
                        }

                        onPegHit(peg)
                    }
                }
            }

            // Bucket collision
            val halfBWidth = bucket.effectiveWidth / 2f
            val bucketTop = bucket.y - bucket.height / 2f
            val bucketBottom = bucket.y + bucket.height / 2f
            val bucketLeft = bucket.x - halfBWidth
            val bucketRight = bucket.x + halfBWidth

            // Check if ball caught in top opening
            if (ball.x >= bucketLeft + 12f && ball.x <= bucketRight - 12f &&
                ball.y + ball.radius >= bucketTop && ball.y <= bucketTop + 16f && ball.vy > 0f
            ) {
                ball.active = false
                return true // Caught!
            }

            // Bucket rim / outer bounce
            if (ball.y + ball.radius >= bucketTop && ball.y - ball.radius <= bucketBottom) {
                if (ball.x + ball.radius >= bucketLeft && ball.x - ball.radius <= bucketRight) {
                    // Rim bounce
                    if (ball.x < bucketLeft + 12f) {
                        ball.x = bucketLeft - ball.radius
                        ball.vx = -kotlin.math.abs(ball.vx) * 0.8f - 40f
                        onRimBounce()
                    } else if (ball.x > bucketRight - 12f) {
                        ball.x = bucketRight + ball.radius
                        ball.vx = kotlin.math.abs(ball.vx) * 0.8f + 40f
                        onRimBounce()
                    }
                }
            }
        }

        return false
    }

    /**
     * Updates bucket horizontal oscillation.
     */
    fun updateBucket(bucket: Bucket, boardWidth: Float, dt: Float) {
        val halfW = bucket.effectiveWidth / 2f
        bucket.x += bucket.vx * dt

        if (bucket.x - halfW < 8f) {
            bucket.x = 8f + halfW
            bucket.vx = kotlin.math.abs(bucket.vx)
        } else if (bucket.x + halfW > boardWidth - 8f) {
            bucket.x = boardWidth - 8f - halfW
            bucket.vx = -kotlin.math.abs(bucket.vx)
        }

        if (bucket.catchAnimTimer > 0f) {
            bucket.catchAnimTimer = (bucket.catchAnimTimer - dt).coerceAtLeast(0f)
        }
    }

    /**
     * Predicts trajectory points for the tactical visual aiming guide line.
     * Computes continuous curved projectile flight under gravity,
     * including realistic elastic reflections off walls, circular pegs, and rotated bricks.
     * With Bjorn's Super Guide, predicts multiple consecutive bounces.
     */
    fun calculateTrajectory(
        startX: Float,
        startY: Float,
        angleRad: Float,
        speed: Float,
        pegs: List<Peg>,
        boardWidth: Float,
        boardHeight: Float,
        superGuide: Boolean = false
    ): List<TrajectoryPoint> {
        val points = mutableListOf<TrajectoryPoint>()
        var simX = startX
        var simY = startY
        var simVx = cos(angleRad) * speed
        var simVy = sin(angleRad) * speed
        val ballRadius = 11f
        // Fine sub-stepping for smooth trajectory curve and precise collision detection
        val stepDt = 0.012f
        val maxSteps = if (superGuide) 240 else 110
        var bouncesLeft = if (superGuide) 4 else 2

        points.add(TrajectoryPoint(simX, simY))

        // Keep track of the last hit peg id to prevent instant re-collision with the same peg in subsequent sub-steps
        var lastHitPegId = -1
        var stepsSinceLastBounce = 0

        for (i in 0 until maxSteps) {
            simVy += gravity * stepDt
            val prevX = simX
            val prevY = simY
            simX += simVx * stepDt
            simY += simVy * stepDt
            stepsSinceLastBounce++

            // 1. Left and Right Wall bounces
            if (simX - ballRadius < 0f) {
                simX = ballRadius
                simVx = kotlin.math.abs(simVx) * restitution
                points.add(
                    TrajectoryPoint(
                        x = simX,
                        y = simY,
                        isBounce = true,
                        bounceType = BounceType.WALL,
                        bounceNormalX = 1f,
                        bounceNormalY = 0f
                    )
                )
                bouncesLeft--
                lastHitPegId = -1
                stepsSinceLastBounce = 0
                if (bouncesLeft <= 0) break
                continue
            } else if (simX + ballRadius > boardWidth) {
                simX = boardWidth - ballRadius
                simVx = -kotlin.math.abs(simVx) * restitution
                points.add(
                    TrajectoryPoint(
                        x = simX,
                        y = simY,
                        isBounce = true,
                        bounceType = BounceType.WALL,
                        bounceNormalX = -1f,
                        bounceNormalY = 0f
                    )
                )
                bouncesLeft--
                lastHitPegId = -1
                stepsSinceLastBounce = 0
                if (bouncesLeft <= 0) break
                continue
            }

            // 2. Peg collision checks (Circle pegs and Rotated Brick pegs)
            var hitOccurred = false
            for (peg in pegs) {
                if (peg.isRemoved) continue
                if (peg.id == lastHitPegId && stepsSinceLastBounce < 3) continue

                if (peg.shape == PegShape.CIRCLE) {
                    val dx = simX - peg.x
                    val dy = simY - peg.y
                    val distSq = dx * dx + dy * dy
                    val minDist = ballRadius + peg.radius

                    if (distSq < minDist * minDist) {
                        val dist = sqrt(distSq).coerceAtLeast(0.001f)
                        val nx = dx / dist
                        val ny = dy / dist

                        // Position ball outside peg surface at exact contact point
                        simX = peg.x + nx * minDist
                        simY = peg.y + ny * minDist

                        val dot = simVx * nx + simVy * ny
                        if (dot < 0f) {
                            simVx -= (1f + restitution) * dot * nx
                            simVy -= (1f + restitution) * dot * ny
                        }

                        points.add(
                            TrajectoryPoint(
                                x = simX,
                                y = simY,
                                isBounce = true,
                                bounceType = BounceType.PEG_CIRCLE,
                                hitPegId = peg.id,
                                bounceNormalX = nx,
                                bounceNormalY = ny
                            )
                        )
                        lastHitPegId = peg.id
                        stepsSinceLastBounce = 0
                        bouncesLeft--
                        hitOccurred = true
                        break
                    }
                } else {
                    // BRICK (oriented bounding box)
                    val rad = Math.toRadians(-peg.rotation.toDouble()).toFloat()
                    val cosA = cos(rad)
                    val sinA = sin(rad)

                    val relX = simX - peg.x
                    val relY = simY - peg.y

                    val localX = relX * cosA - relY * sinA
                    val localY = relX * sinA + relY * cosA

                    val halfW = peg.width / 2f
                    val halfH = peg.height / 2f

                    val clampedX = localX.coerceIn(-halfW, halfW)
                    val clampedY = localY.coerceIn(-halfH, halfH)

                    val diffX = localX - clampedX
                    val diffY = localY - clampedY
                    val distSq = diffX * diffX + diffY * diffY

                    if (distSq < ballRadius * ballRadius) {
                        val dist = sqrt(distSq).coerceAtLeast(0.001f)
                        val localNx = diffX / dist
                        val localNy = diffY / dist

                        val worldRad = Math.toRadians(peg.rotation.toDouble()).toFloat()
                        val wCos = cos(worldRad)
                        val wSin = sin(worldRad)

                        val nx = localNx * wCos - localNy * wSin
                        val ny = localNx * wSin + localNy * wCos

                        // Correct ball position to contact boundary
                        simX += nx * (ballRadius - dist)
                        simY += ny * (ballRadius - dist)

                        val dot = simVx * nx + simVy * ny
                        if (dot < 0f) {
                            simVx -= (1f + restitution) * dot * nx
                            simVy -= (1f + restitution) * dot * ny
                        }

                        points.add(
                            TrajectoryPoint(
                                x = simX,
                                y = simY,
                                isBounce = true,
                                bounceType = BounceType.PEG_BRICK,
                                hitPegId = peg.id,
                                bounceNormalX = nx,
                                bounceNormalY = ny
                            )
                        )
                        lastHitPegId = peg.id
                        stepsSinceLastBounce = 0
                        bouncesLeft--
                        hitOccurred = true
                        break
                    }
                }
            }

            if (hitOccurred && bouncesLeft <= 0) break

            // Add intermediate trajectory samples for smooth path visualization
            if (i % 2 == 0) {
                points.add(TrajectoryPoint(simX, simY))
            }

            // Terminate if ball drops below the board
            if (simY > boardHeight + 30f) break
        }

        return points
    }
}
