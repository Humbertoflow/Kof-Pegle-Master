package com.example.peggle.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.peggle.model.Ball
import com.example.peggle.model.Bucket
import com.example.peggle.model.Particle
import com.example.peggle.model.ParticleType
import com.example.peggle.model.Peg
import com.example.peggle.model.PegShape
import com.example.peggle.model.PegType
import com.example.peggle.model.ScoreBin
import com.example.peggle.model.ScoreFloater
import com.example.peggle.model.ShotPhase
import com.example.peggle.physics.PegglePhysicsEngine
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PeggleBoardCanvas(
    pegs: List<Peg>,
    ball: Ball?,
    bucket: Bucket,
    scoreBins: List<ScoreBin>,
    scoreFloaters: List<ScoreFloater>,
    particles: List<Particle>,
    trajectoryPoints: List<PegglePhysicsEngine.TrajectoryPoint>,
    cannonAngleDeg: Float,
    shotPhase: ShotPhase,
    backgroundGradient: Pair<Long, Long>,
    onAimChange: (Float) -> Unit,
    onFireTap: () -> Unit,
    bgDrawableRes: Int = com.example.R.drawable.bg_anime_forest,
    extraBalls: List<Ball> = emptyList(),
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 1. High-fidelity anime landscape artwork tailored to the current level
        Image(
            painter = painterResource(id = bgDrawableRes),
            contentDescription = "Fondo Anime del Nivel",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Dark cyber-neon scrim overlay for high contrast & 3D neon glow readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF04040A).copy(alpha = 0.50f),
                            Color(0xFF080816).copy(alpha = 0.60f),
                            Color(0xFF04040A).copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // 3. Interactive 3D Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("peggle_board_canvas")
                .pointerInput(shotPhase) {
                    if (shotPhase == ShotPhase.AIMING) {
                        detectTapGestures { offset ->
                            val cannonX = size.width / 2f
                            val cannonY = 32f
                            val dx = offset.x - cannonX
                            val dy = offset.y - cannonY
                            val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            onAimChange(angle)
                            onFireTap()
                        }
                    }
                }
                .pointerInput(shotPhase) {
                    if (shotPhase == ShotPhase.AIMING) {
                        detectDragGestures { change, _ ->
                            val cannonX = size.width / 2f
                            val cannonY = 32f
                            val dx = change.position.x - cannonX
                            val dy = change.position.y - cannonY
                            val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            onAimChange(angle)
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Deep 3D Cyber Space Backdrop & Synthwave Perspective Grid
            draw3DCyberBackground(width, height)

            // 2. Draw 3D Extreme Fever Chambers if in Fever mode
            if (shotPhase == ShotPhase.EXTREME_FEVER || shotPhase == ShotPhase.FEVER_SLOWMO) {
                draw3DNeonScoreBins(scoreBins, width, height)
            }

            // 3. Draw 3D Hover Bucket with Neon Energy Core
            draw3DNeonBucket(bucket, width)

            // 4. Draw 3D Spherical Neon Pegs & Beveled 3D Bricks
            for (peg in pegs) {
                if (!peg.isRemoved) {
                    draw3DNeonPeg(peg)
                }
            }

            // 5. Laser Sight Trajectory Guide & 3D Cannon Turret
            if (shotPhase == ShotPhase.AIMING) {
                drawNeonLaserTrajectory(trajectoryPoints)
            }
            draw3DNeonTurret(cannonAngleDeg, width)

            // 6. Draw 3D Chrome Ball & Glowing Photon Ribbon Trail
            if (ball != null && ball.active) {
                draw3DChromeBall(ball)
            }
            for (eb in extraBalls) {
                if (eb.active) {
                    draw3DChromeBall(eb)
                }
            }

            // 7. Neon Sparks & Laser Fireworks
            drawNeonParticles(particles)

            // 8. Holographic Neon Score Floaters
            drawNeonScoreFloaters(scoreFloaters)
        }
    }
}

/**
 * 3D Cyber Space backdrop with deep dark space and perspective grid lines.
 */
private fun DrawScope.draw3DCyberBackground(width: Float, height: Float) {
    // Subtle cyber gradient vignette over anime backdrop
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0xFF04040A).copy(alpha = 0.25f),
                Color(0xFF080414).copy(alpha = 0.45f)
            )
        ),
        size = size
    )

    // Ambient top-down cyan and purple neon glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF00F0FF).copy(alpha = 0.12f), Color.Transparent),
            center = Offset(width / 2f, 40f),
            radius = width * 0.7f
        ),
        radius = width * 0.7f,
        center = Offset(width / 2f, 40f)
    )

    // Lower synthwave grid for 3D depth
    val horizonY = height * 0.72f
    val gridColor = Color(0xFFB026FF).copy(alpha = 0.22f)
    val gridColorStrong = Color(0xFF00F0FF).copy(alpha = 0.35f)

    // Horizontal perspective grid lines
    val steps = 8
    for (i in 1..steps) {
        val t = (i.toFloat() / steps)
        val py = horizonY + (height - horizonY) * (t * t)
        drawLine(
            color = if (i % 2 == 0) gridColorStrong else gridColor,
            start = Offset(0f, py),
            end = Offset(width, py),
            strokeWidth = 1f + t * 1.5f
        )
    }

    // Converging perspective lines toward vanishing point
    val vanishX = width / 2f
    val vanishY = horizonY - 40f
    val perspectiveCount = 9
    for (i in 0..perspectiveCount) {
        val bottomX = (width / perspectiveCount) * i
        drawLine(
            color = gridColor,
            start = Offset(vanishX, vanishY),
            end = Offset(bottomX, height),
            strokeWidth = 1.2f
        )
    }

    // Laser side rails (boundary light guides)
    drawLine(
        brush = Brush.verticalGradient(listOf(Color(0xFF00F0FF), Color(0xFFFF007F))),
        start = Offset(2f, 0f),
        end = Offset(2f, height),
        strokeWidth = 3f
    )
    drawLine(
        brush = Brush.verticalGradient(listOf(Color(0xFF00F0FF), Color(0xFFFF007F))),
        start = Offset(width - 2f, 0f),
        end = Offset(width - 2f, height),
        strokeWidth = 3f
    )
}

/**
 * 3D Spherical Neon Peg with layered bloom, fresnel rim, and high-gloss specular highlight.
 */
private fun DrawScope.draw3DNeonPeg(peg: Peg) {
    val (neonGlow, neonCore, neonHighlight) = when (peg.type) {
        PegType.ORANGE -> Triple(Color(0xFFFF5E00), Color(0xFFFF8A00), Color(0xFFFFD54F))
        PegType.BLUE -> Triple(Color(0xFF00E5FF), Color(0xFF00B0FF), Color(0xFFE0F7FA))
        PegType.GREEN -> Triple(Color(0xFF00FF66), Color(0xFF00E676), Color(0xFFB9F6CA))
        PegType.PURPLE -> Triple(Color(0xFFFF00CC), Color(0xFFE040FB), Color(0xFFF8BBD0))
        PegType.MULTIBALL -> Triple(Color(0xFF00E5FF), Color(0xFF00F5FF), Color(0xFFE0FFFF))
        PegType.LONG_AIM -> Triple(Color(0xFFFFD700), Color(0xFFFFEA00), Color(0xFFFFFDE7))
        PegType.BLAST_RADIUS -> Triple(Color(0xFFFF2A2A), Color(0xFFFF4500), Color(0xFFFFCCBC))
    }

    if (peg.shape == PegShape.CIRCLE) {
        val center = Offset(peg.x, peg.y)
        val r = peg.radius

        // 1. Layered Neon Bloom Halo
        val bloomAlpha = if (peg.isHit) 0.85f else 0.35f
        drawCircle(
            color = neonGlow.copy(alpha = bloomAlpha * 0.4f),
            radius = r + 9f,
            center = center
        )
        drawCircle(
            color = neonGlow.copy(alpha = bloomAlpha * 0.7f),
            radius = r + 4f,
            center = center
        )

        // 2. 3D Under-drop Shadow (creates elevation above board)
        drawCircle(
            color = Color.Black.copy(alpha = 0.65f),
            radius = r,
            center = Offset(peg.x + 2.5f, peg.y + 2.5f)
        )

        // 3. 3D Spherical Radial Body
        val lightOffset = Offset(peg.x - r * 0.32f, peg.y - r * 0.32f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    if (peg.isHit) Color.White else neonHighlight,
                    neonCore,
                    neonGlow,
                    Color(0xFF0A0A1A)
                ),
                center = lightOffset,
                radius = r * 1.35f
            ),
            radius = r,
            center = center
        )

        // 4. Glowing 3D Glass Rim / Fresnel Ring
        drawCircle(
            brush = Brush.sweepGradient(
                listOf(
                    neonHighlight,
                    neonGlow,
                    Color.White.copy(alpha = 0.9f),
                    neonGlow,
                    neonHighlight
                ),
                center = center
            ),
            radius = r,
            center = center,
            style = Stroke(width = if (peg.isHit) 2.5f else 1.8f)
        )

        // Power-Up Tactical Icons/Glyphs inside circle
        when (peg.type) {
            PegType.MULTIBALL -> {
                // Dual ball spheres icon inside peg
                drawCircle(color = Color.White, radius = r * 0.22f, center = Offset(peg.x - r * 0.28f, peg.y))
                drawCircle(color = Color(0xFF00F5FF), radius = r * 0.22f, center = Offset(peg.x + r * 0.28f, peg.y))
            }
            PegType.LONG_AIM -> {
                // Crosshair / target reticle inside peg
                drawLine(
                    color = Color.White,
                    start = Offset(peg.x - r * 0.5f, peg.y),
                    end = Offset(peg.x + r * 0.5f, peg.y),
                    strokeWidth = 1.5f
                )
                drawLine(
                    color = Color.White,
                    start = Offset(peg.x, peg.y - r * 0.5f),
                    end = Offset(peg.x, peg.y + r * 0.5f),
                    strokeWidth = 1.5f
                )
                drawCircle(color = Color.White, radius = r * 0.25f, center = center, style = Stroke(width = 1.2f))
            }
            PegType.BLAST_RADIUS -> {
                // 4-point energy blast star
                val starR = r * 0.45f
                drawLine(color = Color.White, start = Offset(peg.x - starR, peg.y), end = Offset(peg.x + starR, peg.y), strokeWidth = 2.0f)
                drawLine(color = Color.White, start = Offset(peg.x, peg.y - starR), end = Offset(peg.x, peg.y + starR), strokeWidth = 2.0f)
                drawLine(color = Color(0xFFFFD54F), start = Offset(peg.x - starR * 0.6f, peg.y - starR * 0.6f), end = Offset(peg.x + starR * 0.6f, peg.y + starR * 0.6f), strokeWidth = 1.4f)
                drawLine(color = Color(0xFFFFD54F), start = Offset(peg.x + starR * 0.6f, peg.y - starR * 0.6f), end = Offset(peg.x - starR * 0.6f, peg.y + starR * 0.6f), strokeWidth = 1.4f)
            }
            else -> {}
        }

        // 5. Specular 3D Reflection Glint (top-left glossy curved reflection)
        drawCircle(
            color = Color.White.copy(alpha = 0.95f),
            radius = r * 0.28f,
            center = Offset(peg.x - r * 0.35f, peg.y - r * 0.35f)
        )

        // Subtle secondary bounced reflection at bottom-right
        drawCircle(
            color = neonHighlight.copy(alpha = 0.45f),
            radius = r * 0.16f,
            center = Offset(peg.x + r * 0.35f, peg.y + r * 0.35f)
        )
    } else {
        // 3D Beveled Brick
        rotate(degrees = peg.rotation, pivot = Offset(peg.x, peg.y)) {
            val halfW = peg.width / 2f
            val halfH = peg.height / 2f
            val w = peg.width
            val h = peg.height
            val corner = CornerRadius(4f, 4f)

            // 1. 3D Extrusion Depth Underlay (isometric shadow block)
            drawRoundRect(
                color = Color(0xFF050510).copy(alpha = 0.85f),
                topLeft = Offset(peg.x - halfW + 2f, peg.y - halfH + 3.5f),
                size = Size(w, h),
                cornerRadius = corner
            )

            // 2. Outer Neon Bloom
            val bloomAlpha = if (peg.isHit) 0.7f else 0.3f
            drawRoundRect(
                color = neonGlow.copy(alpha = bloomAlpha),
                topLeft = Offset(peg.x - halfW - 3f, peg.y - halfH - 3f),
                size = Size(w + 6f, h + 6f),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // 3. Top Face with 3D Light Gradient
            val topFaceOrigin = Offset(peg.x - halfW, peg.y - halfH)
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        if (peg.isHit) Color.White else neonHighlight,
                        neonCore,
                        Color(0xFF080816)
                    ),
                    start = topFaceOrigin,
                    end = Offset(topFaceOrigin.x, topFaceOrigin.y + h)
                ),
                topLeft = topFaceOrigin,
                size = Size(w, h),
                cornerRadius = corner
            )

            // 4. Sharp Neon Wireframe Laser Border
            drawRoundRect(
                color = if (peg.isHit) Color.White else neonHighlight,
                topLeft = topFaceOrigin,
                size = Size(w, h),
                cornerRadius = corner,
                style = Stroke(width = 1.6f)
            )

            // 5. Bevel highlight strip along top edge
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(topFaceOrigin.x + 3f, topFaceOrigin.y + 1f),
                end = Offset(topFaceOrigin.x + w - 3f, topFaceOrigin.y + 1f),
                strokeWidth = 1.5f
            )
        }
    }
}

/**
 * 3D Chrome Ball with high-contrast specular reflection and glowing photon trail.
 */
private fun DrawScope.draw3DChromeBall(ball: Ball) {
    // 1. Glowing Neon Photon Ribbon Trail
    val trailSize = ball.trail.size
    ball.trail.forEachIndexed { index, (tx, ty) ->
        val progress = index.toFloat() / trailSize
        val alpha = progress * 0.5f
        val rad = ball.radius * (0.35f + 0.65f * progress)

        // Cyan-magenta neon energy stream
        val trailColor = if (ball.spookyRespawnsLeft > 0) Color(0xFFFF5E00) else Color(0xFF00F0FF)
        drawCircle(
            color = trailColor.copy(alpha = alpha * 0.4f),
            radius = rad + 4f,
            center = Offset(tx, ty)
        )
        drawCircle(
            color = trailColor.copy(alpha = alpha),
            radius = rad,
            center = Offset(tx, ty)
        )
    }

    // 2. Ball 3D Drop Shadow
    drawCircle(
        color = Color.Black.copy(alpha = 0.7f),
        radius = ball.radius,
        center = Offset(ball.x + 3f, ball.y + 3f)
    )

    // 3. Mirror Chrome Sphere Shader
    val lightCenter = Offset(ball.x - ball.radius * 0.35f, ball.y - ball.radius * 0.35f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White,
                Color(0xFFE0F7FA),
                Color(0xFF80DEEA),
                Color(0xFF0097A7),
                Color(0xFF001E2E)
            ),
            center = lightCenter,
            radius = ball.radius * 1.3f
        ),
        radius = ball.radius,
        center = Offset(ball.x, ball.y)
    )

    // 4. Glowing Neon Rim Glow on Chrome Sphere
    drawCircle(
        color = Color(0xFF00F0FF),
        radius = ball.radius,
        center = Offset(ball.x, ball.y),
        style = Stroke(width = 1.8f)
    )

    // 5. Double Pin-point Specular Highlight Glint
    drawCircle(
        color = Color.White,
        radius = ball.radius * 0.32f,
        center = Offset(ball.x - ball.radius * 0.35f, ball.y - ball.radius * 0.35f)
    )
    drawCircle(
        color = Color(0xFFE040FB).copy(alpha = 0.6f),
        radius = ball.radius * 0.18f,
        center = Offset(ball.x + ball.radius * 0.35f, ball.y + ball.radius * 0.35f)
    )
}

/**
 * 3D Cyber Hover-Catcher Bucket with glowing laser rails and holographic wings.
 */
private fun DrawScope.draw3DNeonBucket(bucket: Bucket, screenWidth: Float) {
    val halfW = bucket.effectiveWidth / 2f
    val bX = bucket.x
    val bY = bucket.y
    val bW = bucket.effectiveWidth
    val bH = bucket.height

    // Kat Tut 3D Holographic Laser Wings
    if (bucket.hasPyramidWings) {
        val wingW = 36f
        // Left holographic laser wing
        val leftWing = Path().apply {
            moveTo(bX - halfW, bY - 2f)
            lineTo(bX - halfW - wingW, bY - 10f)
            lineTo(bX - halfW - wingW * 0.4f, bY + bH + 4f)
            lineTo(bX - halfW, bY + bH)
            close()
        }
        drawPath(leftWing, brush = Brush.linearGradient(listOf(Color(0xFFFFEA00).copy(alpha = 0.7f), Color(0xFFFF5E00).copy(alpha = 0.2f))))
        drawPath(leftWing, color = Color(0xFFFFEA00), style = Stroke(width = 2f))

        // Right holographic laser wing
        val rightWing = Path().apply {
            moveTo(bX + halfW, bY - 2f)
            lineTo(bX + halfW + wingW, bY - 10f)
            lineTo(bX + halfW + wingW * 0.4f, bY + bH + 4f)
            lineTo(bX + halfW, bY + bH)
            close()
        }
        drawPath(rightWing, brush = Brush.linearGradient(listOf(Color(0xFFFFEA00).copy(alpha = 0.7f), Color(0xFFFF5E00).copy(alpha = 0.2f))))
        drawPath(rightWing, color = Color(0xFFFFEA00), style = Stroke(width = 2f))
    }

    // Bucket Floor Glow / Propulsion Jets
    drawRoundRect(
        color = Color(0xFF00F0FF).copy(alpha = 0.35f),
        topLeft = Offset(bX - halfW + 10f, bY + bH / 2f),
        size = Size(bW - 20f, 8f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Outer Neon Bloom when catching ball
    if (bucket.catchAnimTimer > 0f) {
        drawRoundRect(
            color = Color(0xFFFFEA00).copy(alpha = 0.75f),
            topLeft = Offset(bX - halfW - 8f, bY - bH / 2f - 6f),
            size = Size(bW + 16f, bH + 12f),
            cornerRadius = CornerRadius(14f, 14f)
        )
    }

    // 3D Chassis Body
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF14142B),
                Color(0xFF0A0A16),
                Color(0xFF003B5C)
            )
        ),
        topLeft = Offset(bX - halfW, bY - bH / 2f),
        size = Size(bW, bH),
        cornerRadius = CornerRadius(10f, 10f)
    )

    // Glowing Neon Top Laser Rail
    drawRoundRect(
        brush = Brush.horizontalGradient(
            listOf(Color(0xFF00F0FF), Color(0xFFB026FF), Color(0xFF00F0FF))
        ),
        topLeft = Offset(bX - halfW, bY - bH / 2f),
        size = Size(bW, 5f),
        cornerRadius = CornerRadius(3f, 3f)
    )

    // Inner Catch Chamber (Dark void portal)
    drawRoundRect(
        color = Color(0xFF05050D),
        topLeft = Offset(bX - halfW + 12f, bY - bH / 2f + 4f),
        size = Size(bW - 24f, bH - 8f),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Central Neon Power Node
    drawCircle(
        color = if (bucket.catchAnimTimer > 0f) Color(0xFFFFEA00) else Color(0xFF00FF66),
        radius = 5f,
        center = Offset(bX, bY + 2f)
    )
    drawCircle(
        color = Color.White,
        radius = 2f,
        center = Offset(bX, bY + 2f)
    )

    // Neon Edge Trim
    drawRoundRect(
        color = Color(0xFF00F0FF),
        topLeft = Offset(bX - halfW, bY - bH / 2f),
        size = Size(bW, bH),
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(width = 1.6f)
    )
}

/**
 * 3D Cyber Cannon Turret with glowing energy rings and plasma emitter.
 */
private fun DrawScope.draw3DNeonTurret(angleDeg: Float, screenWidth: Float) {
    val turretX = screenWidth / 2f
    val turretY = 32f
    val barrelLength = 50f
    val barrelWidth = 26f

    rotate(degrees = angleDeg - 90f, pivot = Offset(turretX, turretY)) {
        // Barrel Under-shadow
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.5f),
            topLeft = Offset(turretX - barrelWidth / 2f + 2f, turretY + 3f),
            size = Size(barrelWidth, barrelLength),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // 3D Metallic Barrel Body
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF0C101D),
                    Color(0xFF2E3852),
                    Color(0xFF5D6B8F),
                    Color(0xFF1E2436)
                )
            ),
            topLeft = Offset(turretX - barrelWidth / 2f, turretY),
            size = Size(barrelWidth, barrelLength),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Glowing Neon Energy Accents along barrel
        drawLine(
            color = Color(0xFF00F0FF),
            start = Offset(turretX - barrelWidth / 2f + 3f, turretY + 8f),
            end = Offset(turretX - barrelWidth / 2f + 3f, turretY + barrelLength - 8f),
            strokeWidth = 2f
        )
        drawLine(
            color = Color(0xFF00F0FF),
            start = Offset(turretX + barrelWidth / 2f - 3f, turretY + 8f),
            end = Offset(turretX + barrelWidth / 2f - 3f, turretY + barrelLength - 8f),
            strokeWidth = 2f
        )

        // Neon Muzzle Ring Emitter
        drawRoundRect(
            color = Color(0xFFFF007F),
            topLeft = Offset(turretX - barrelWidth / 2f - 2f, turretY + barrelLength - 8f),
            size = Size(barrelWidth + 4f, 8f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(turretX - barrelWidth / 2f, turretY + barrelLength - 6f),
            size = Size(barrelWidth, 4f),
            cornerRadius = CornerRadius(2f, 2f)
        )
    }

    // 3D Turret Base Orb with Neon Ring
    drawCircle(
        color = Color(0xFF00F0FF).copy(alpha = 0.4f),
        radius = 32f,
        center = Offset(turretX, turretY)
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF4A5568),
                Color(0xFF1A202C),
                Color(0xFF0A0D14)
            ),
            center = Offset(turretX - 8f, turretY - 8f),
            radius = 28f
        ),
        radius = 26f,
        center = Offset(turretX, turretY)
    )

    // Outer laser ring
    drawCircle(
        color = Color(0xFF00F0FF),
        radius = 26f,
        center = Offset(turretX, turretY),
        style = Stroke(width = 2f)
    )

    // Glowing Neon Center Eye / Core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, Color(0xFF00F0FF), Color(0xFF0052CC)),
            center = Offset(turretX - 3f, turretY - 3f),
            radius = 12f
        ),
        radius = 11f,
        center = Offset(turretX, turretY)
    )
}

/**
 * Visual Trajectory Line and Laser Sight with neon continuous path,
 * predicted bounces off walls and pegs, pulsating crosshairs, and directional arrows.
 */
private fun DrawScope.drawNeonLaserTrajectory(points: List<PegglePhysicsEngine.TrajectoryPoint>) {
    if (points.size < 2) return

    // Partition points into bounce segments for distinct gradient coloring (e.g. primary shot -> first bounce -> second bounce)
    val segments = mutableListOf<MutableList<PegglePhysicsEngine.TrajectoryPoint>>()
    var currentSegment = mutableListOf<PegglePhysicsEngine.TrajectoryPoint>()
    currentSegment.add(points.first())

    for (i in 1 until points.size) {
        val pt = points[i]
        currentSegment.add(pt)
        if (pt.isBounce) {
            segments.add(currentSegment)
            currentSegment = mutableListOf()
            currentSegment.add(pt)
        }
    }
    if (currentSegment.size > 1) {
        segments.add(currentSegment)
    }

    // Segment color schemes for tactical bounce reading
    val segmentColors = listOf(
        Pair(Color(0xFF00F0FF), Color(0xFF00A2FF)), // Segment 0: Cyan / Electric Blue (Direct aim)
        Pair(Color(0xFFFF007F), Color(0xFFFF5E00)), // Segment 1: Neon Magenta / Orange (1st Ricochet)
        Pair(Color(0xFF39FF14), Color(0xFF00F0FF)), // Segment 2: Neon Lime / Cyan (2nd Ricochet)
        Pair(Color(0xFFFFDF00), Color(0xFFFF007F))  // Segment 3: Electric Gold / Pink (3rd Ricochet)
    )

    // 1. Draw continuous connecting laser trajectory path
    segments.forEachIndexed { segIndex, segmentPoints ->
        if (segmentPoints.size >= 2) {
            val path = Path().apply {
                moveTo(segmentPoints[0].x, segmentPoints[0].y)
                for (j in 1 until segmentPoints.size) {
                    lineTo(segmentPoints[j].x, segmentPoints[j].y)
                }
            }

            val (startColor, endColor) = segmentColors.getOrElse(segIndex) { segmentColors.last() }
            val segAlpha = (1f - (segIndex * 0.22f)).coerceIn(0.4f, 1f)

            // Outer laser bloom / glow line
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(
                        startColor.copy(alpha = 0.35f * segAlpha),
                        endColor.copy(alpha = 0.15f * segAlpha)
                    ),
                    start = Offset(segmentPoints.first().x, segmentPoints.first().y),
                    end = Offset(segmentPoints.last().x, segmentPoints.last().y)
                ),
                style = Stroke(
                    width = 7f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                )
            )

            // Core high-intensity laser line
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(
                        startColor.copy(alpha = 0.90f * segAlpha),
                        endColor.copy(alpha = 0.65f * segAlpha)
                    ),
                    start = Offset(segmentPoints.first().x, segmentPoints.first().y),
                    end = Offset(segmentPoints.last().x, segmentPoints.last().y)
                ),
                style = Stroke(
                    width = 2.8f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                )
            )

            // Pure white hyper-bright laser core dash
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.85f * segAlpha),
                style = Stroke(
                    width = 1.2f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 16f), 0f)
                )
            )
        }
    }

    // 2. Draw trajectory dots, bounce impact indicators, and crosshair rings
    for (i in points.indices) {
        val pt = points[i]
        val progress = i.toFloat() / points.size
        val alpha = (1f - progress * 0.65f).coerceIn(0.35f, 1f)

        if (pt.isBounce) {
            val isWall = pt.bounceType == PegglePhysicsEngine.BounceType.WALL
            val bounceColor = if (isWall) Color(0xFFFFDF00) else Color(0xFFFF007F)

            // Outer glowing impact burst ring
            drawCircle(
                color = bounceColor.copy(alpha = 0.35f),
                radius = 12f,
                center = Offset(pt.x, pt.y)
            )
            // Mid precision tactical reticle ring
            drawCircle(
                color = if (isWall) Color(0xFFFF9E00) else Color(0xFF00F0FF),
                radius = 8.5f,
                center = Offset(pt.x, pt.y),
                style = Stroke(width = 2.2f)
            )
            // Inner core impact spark
            drawCircle(
                color = Color.White,
                radius = 3.5f,
                center = Offset(pt.x, pt.y)
            )

            // Crosshair tick marks for tactical aim alignment
            val tickLen = 6f
            val cx = pt.x
            val cy = pt.y
            drawLine(
                color = Color.White.copy(alpha = 0.9f),
                start = Offset(cx - tickLen - 5f, cy),
                end = Offset(cx - 5f, cy),
                strokeWidth = 1.8f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.9f),
                start = Offset(cx + 5f, cy),
                end = Offset(cx + tickLen + 5f, cy),
                strokeWidth = 1.8f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.9f),
                start = Offset(cx, cy - tickLen - 5f),
                end = Offset(cx, cy - 5f),
                strokeWidth = 1.8f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.9f),
                start = Offset(cx, cy + 5f),
                end = Offset(cx, cy + tickLen + 5f),
                strokeWidth = 1.8f
            )
        } else if (i % 2 == 0) {
            // Glowing neon laser photon dot along the trajectory
            drawCircle(
                color = Color(0xFF00F0FF).copy(alpha = alpha * 0.45f),
                radius = 5.0f,
                center = Offset(pt.x, pt.y)
            )
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.95f),
                radius = 2.5f,
                center = Offset(pt.x, pt.y)
            )
        }
    }

    // 3. Terminal aiming bead at the end of the trajectory line
    val lastPt = points.last()
    drawCircle(
        color = Color(0xFF00F0FF).copy(alpha = 0.4f),
        radius = 8f,
        center = Offset(lastPt.x, lastPt.y)
    )
    drawCircle(
        color = Color.White,
        radius = 3f,
        center = Offset(lastPt.x, lastPt.y)
    )
}

/**
 * 3D Neon Score Bins in Extreme Fever mode.
 */
private fun DrawScope.draw3DNeonScoreBins(scoreBins: List<ScoreBin>, width: Float, height: Float) {
    val binHeight = 56f
    val topY = height - binHeight

    for (bin in scoreBins) {
        val startX = bin.minXRatio * width
        val endX = bin.maxXRatio * width
        val binW = endX - startX

        val neonColor = Color(bin.color)

        // 3D Glass Chamber
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    neonColor.copy(alpha = 0.5f),
                    Color(0xFF06060F)
                )
            ),
            topLeft = Offset(startX, topY),
            size = Size(binW, binHeight)
        )

        // Top laser edge
        drawLine(
            color = neonColor,
            start = Offset(startX, topY),
            end = Offset(endX, topY),
            strokeWidth = 3f
        )

        // Wireframe partitions
        drawRect(
            color = neonColor.copy(alpha = 0.7f),
            topLeft = Offset(startX, topY),
            size = Size(binW, binHeight),
            style = Stroke(width = 1.5f)
        )

        // Luminous digital text
        drawContext.canvas.nativeCanvas.apply {
            val paint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 28f
                isAntiAlias = true
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
                setShadowLayer(10f, 0f, 0f, neonColor.hashCode())
            }
            drawText(bin.label, startX + binW / 2f, topY + 36f, paint)
        }
    }
}

/**
 * Neon particles, energetic shockwaves, and spark flashes on peg impacts.
 */
private fun DrawScope.drawNeonParticles(particles: List<Particle>) {
    for (p in particles) {
        val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
        val color = Color(p.color)

        when (p.type) {
            ParticleType.SHOCKWAVE_RING -> {
                // Expanding energetic impact shockwave ring
                val ringStroke = (3.5f * alpha).coerceAtLeast(1.2f)
                drawCircle(
                    color = color.copy(alpha = alpha * 0.75f),
                    radius = p.size,
                    center = Offset(p.x, p.y),
                    style = Stroke(width = ringStroke)
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.9f),
                    radius = (p.size * 0.96f).coerceAtLeast(1f),
                    center = Offset(p.x, p.y),
                    style = Stroke(width = 1.2f)
                )
            }
            ParticleType.STAR_SPARKLE -> {
                // Rotating 4-pointed radiant sparkle star
                rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
                    val armLength = p.size * 1.5f
                    val armWidth = (p.size * 0.35f).coerceAtLeast(1.5f)
                    drawLine(
                        color = color.copy(alpha = alpha * 0.85f),
                        start = Offset(p.x - armLength, p.y),
                        end = Offset(p.x + armLength, p.y),
                        strokeWidth = armWidth
                    )
                    drawLine(
                        color = color.copy(alpha = alpha * 0.85f),
                        start = Offset(p.x, p.y - armLength),
                        end = Offset(p.x, p.y + armLength),
                        strokeWidth = armWidth
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = p.size * 0.55f,
                        center = Offset(p.x, p.y)
                    )
                }
            }
            ParticleType.GLOW_EMBER -> {
                // Soft floating warm flame ember
                drawCircle(
                    color = color.copy(alpha = alpha * 0.35f),
                    radius = p.size * 2.2f,
                    center = Offset(p.x, p.y)
                )
                drawCircle(
                    color = color.copy(alpha = alpha * 0.7f),
                    radius = p.size * 1.2f,
                    center = Offset(p.x, p.y)
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.95f),
                    radius = p.size * 0.45f,
                    center = Offset(p.x, p.y)
                )
            }
            ParticleType.SPARK -> {
                // High-velocity impact spark streak
                val speed = kotlin.math.hypot(p.vx, p.vy)
                if (speed > 80f) {
                    val trailLength = (speed * 0.04f).coerceIn(4f, 22f)
                    val dirX = p.vx / speed
                    val dirY = p.vy / speed
                    drawLine(
                        color = color.copy(alpha = alpha * 0.85f),
                        start = Offset(p.x, p.y),
                        end = Offset(p.x - dirX * trailLength, p.y - dirY * trailLength),
                        strokeWidth = (p.size * 0.8f).coerceAtLeast(2f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = p.size * 0.5f,
                        center = Offset(p.x, p.y)
                    )
                } else {
                    // Outer glow
                    drawCircle(
                        color = color.copy(alpha = alpha * 0.45f),
                        radius = p.size * 1.8f,
                        center = Offset(p.x, p.y)
                    )
                    // Core spark
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = p.size * 0.6f,
                        center = Offset(p.x, p.y)
                    )
                }
            }
        }
    }
}

/**
 * Holographic score popups with neon laser glow.
 */
private fun DrawScope.drawNeonScoreFloaters(floaters: List<ScoreFloater>) {
    for (f in floaters) {
        drawContext.canvas.nativeCanvas.apply {
            val textColor = Color(f.color)
            val paint = Paint().apply {
                color = if (f.isStyleShot) android.graphics.Color.WHITE else textColor.hashCode()
                textSize = if (f.isStyleShot) 40f else 32f
                isAntiAlias = true
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
                setShadowLayer(14f, 0f, 0f, textColor.hashCode())
            }
            drawText(f.text, f.x, f.y, paint)
        }
    }
}
