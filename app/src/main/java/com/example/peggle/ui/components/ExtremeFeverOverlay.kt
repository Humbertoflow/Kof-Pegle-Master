package com.example.peggle.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonYellow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class FeverParticleKind {
    SPARK,
    STAR,
    CONFETTI,
    SHOCKWAVE
}

data class CelebrationParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var size: Float,
    var life: Float,
    val maxLife: Float,
    val kind: FeverParticleKind,
    var rotation: Float = 0f,
    var vRot: Float = 0f
)

private val CELEBRATION_PALETTE = listOf(
    NeonOrange,
    NeonYellow,
    NeonMagenta,
    NeonCyan,
    NeonGreen,
    Color(0xFFFFD700), // Gold
    Color(0xFFFF3366), // Hot Red-Pink
    Color(0xFF00FFCC)  // Bright Turquoise
)

/**
 * High-energy Extreme Fever celebration overlay with multi-burst particle explosions,
 * glowing neon banners, and comprehensive victory scoring breakdown.
 */
@Composable
fun ExtremeFeverOverlay(
    score: Int,
    stars: Int,
    isMasterClear: Boolean,
    feverBinScore: Int,
    ballsLeftBonus: Int,
    onNextLevel: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit,
    onOpenMap: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glow_rot"
    )

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.78f))
                .testTag("extreme_fever_dialog"),
            contentAlignment = Alignment.Center
        ) {
            val widthPx = constraints.maxWidth.toFloat().coerceAtLeast(300f)
            val heightPx = constraints.maxHeight.toFloat().coerceAtLeast(400f)

            // Animated particle explosions engine
            val particles = remember { mutableStateListOf<CelebrationParticle>() }
            var frameTrigger by remember { mutableLongStateOf(0L) }

            fun spawnExplosion(centerX: Float, centerY: Float, particleCount: Int = 36) {
                // Expanding shockwave ring
                val ringColor = CELEBRATION_PALETTE.random()
                particles.add(
                    CelebrationParticle(
                        x = centerX,
                        y = centerY,
                        vx = 0f,
                        vy = 0f,
                        color = ringColor,
                        size = 8f,
                        life = 1f,
                        maxLife = 1f,
                        kind = FeverParticleKind.SHOCKWAVE
                    )
                )

                // High-velocity sparks and stars
                for (i in 0 until particleCount) {
                    val angle = (2 * PI * i / particleCount) + (Random.nextFloat() * 0.3f)
                    val speed = 250f + Random.nextFloat() * 450f
                    val color = CELEBRATION_PALETTE.random()
                    val kind = when (Random.nextInt(4)) {
                        0 -> FeverParticleKind.STAR
                        1, 2 -> FeverParticleKind.SPARK
                        else -> FeverParticleKind.CONFETTI
                    }
                    val size = when (kind) {
                        FeverParticleKind.STAR -> 12f + Random.nextFloat() * 8f
                        FeverParticleKind.CONFETTI -> 10f + Random.nextFloat() * 8f
                        else -> 5f + Random.nextFloat() * 6f
                    }
                    val maxLife = 0.8f + Random.nextFloat() * 0.9f

                    particles.add(
                        CelebrationParticle(
                            x = centerX,
                            y = centerY,
                            vx = (cos(angle) * speed).toFloat(),
                            vy = (sin(angle) * speed).toFloat(),
                            color = color,
                            size = size,
                            life = maxLife,
                            maxLife = maxLife,
                            kind = kind,
                            rotation = Random.nextFloat() * 360f,
                            vRot = (Random.nextFloat() - 0.5f) * 400f
                        )
                    )
                }
            }

            // Continuous animation and periodic fireworks detonation loop
            LaunchedEffect(widthPx, heightPx) {
                // Initial massive multi-burst detonations
                spawnExplosion(widthPx * 0.25f, heightPx * 0.25f, 40)
                spawnExplosion(widthPx * 0.75f, heightPx * 0.28f, 40)
                spawnExplosion(widthPx * 0.5f, heightPx * 0.18f, 48)

                var lastBurstNanos = System.nanoTime()
                var lastFrameNanos = System.nanoTime()

                while (true) {
                    withFrameNanos { nowNanos ->
                        val dt = ((nowNanos - lastFrameNanos) / 1_000_000_000.0f).coerceIn(0.001f, 0.05f)
                        lastFrameNanos = nowNanos

                        // Spontaneous explosive firework detonations across the screen
                        if (nowNanos - lastBurstNanos > 380_000_000L) { // every 380ms
                            lastBurstNanos = nowNanos
                            val bx = widthPx * (0.15f + Random.nextFloat() * 0.7f)
                            val by = heightPx * (0.12f + Random.nextFloat() * 0.76f)
                            spawnExplosion(bx, by, 32)
                        }

                        // Update existing particles
                        val iter = particles.iterator()
                        while (iter.hasNext()) {
                            val p = iter.next()
                            p.life -= dt
                            if (p.life <= 0f) {
                                iter.remove()
                                continue
                            }

                            when (p.kind) {
                                FeverParticleKind.SHOCKWAVE -> {
                                    p.size += 280f * dt
                                }
                                FeverParticleKind.CONFETTI -> {
                                    p.x += p.vx * dt
                                    p.y += p.vy * dt
                                    p.vy += 120f * dt // gentle gravity
                                    p.vx *= (1f - 0.4f * dt)
                                    p.rotation += p.vRot * dt
                                }
                                FeverParticleKind.SPARK, FeverParticleKind.STAR -> {
                                    p.x += p.vx * dt
                                    p.y += p.vy * dt
                                    p.vy += 180f * dt // gravity
                                    p.vx *= (1f - 0.6f * dt) // air drag
                                    p.rotation += p.vRot * dt
                                }
                            }
                        }

                        frameTrigger = nowNanos
                    }
                }
            }

            // Particle Explosions Canvas Layer (draws behind and around the dialogue card)
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Read frameTrigger state to ensure continuous recomposition
                @Suppress("UNUSED_VARIABLE")
                val trigger = frameTrigger

                for (p in particles) {
                    drawCelebrationParticle(p)
                }
            }

            // Central High-Energy Victory Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 440.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xEB070714)),
                border = androidx.compose.foundation.BorderStroke(
                    2.5.dp,
                    Brush.sweepGradient(
                        listOf(NeonMagenta, NeonOrange, NeonYellow, NeonCyan, NeonMagenta)
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Explosive High-Energy Title Banner
                    Box(
                        modifier = Modifier
                            .scale(pulseScale)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(NeonMagenta, NeonOrange, NeonYellow)
                                )
                            )
                            .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "⚡ EXTREME FEVER! ⚡",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            letterSpacing = 1.2.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "¡TODOS LOS PEGS NARANJAS DESTRUIDOS!",
                        color = NeonYellow,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "🎶 Himno a la Alegría • Beethoven 🎶",
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stars Achievement Row
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..3) {
                            val earned = i <= stars
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Estrella $i",
                                tint = if (earned) NeonYellow else Color(0xFF1E2442),
                                modifier = Modifier
                                    .size(if (i == 2) 48.dp else 38.dp)
                                    .scale(if (earned) 1.05f else 1.0f)
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Score HUD Summary Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF0E1026))
                            .border(1.5.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PUNTUACIÓN TOTAL",
                            color = Color(0xFF8B9CB5),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = String.format("%,d", score),
                            color = NeonYellow,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cámara de Fiebre:",
                                color = Color(0xFFCBD5E1),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "+${String.format("%,d", feverBinScore)} pts",
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bolas Restantes Bonus:",
                                color = Color(0xFFCBD5E1),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "+${String.format("%,d", ballsLeftBonus)} pts",
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        if (isMasterClear) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NeonGreen.copy(alpha = 0.18f))
                                    .border(1.5.dp, NeonGreen, RoundedCornerShape(10.dp))
                                    .padding(vertical = 6.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🏆 ¡DESPEJE MAESTRO 100%! +100,000 PTS",
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Action Buttons
                    Button(
                        onClick = onNextLevel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("next_level_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)
                    ) {
                        Text(
                            text = "Siguiente Nivel",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRestart,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("fever_restart_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan)
                        ) {
                            Text(
                                text = "Reintentar",
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        if (onOpenMap != null) {
                            OutlinedButton(
                                onClick = onOpenMap,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("fever_map_button"),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700))
                            ) {
                                Text(
                                    text = "Mapa",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onHome,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("fever_home_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF64748B))
                        ) {
                            Text(
                                text = "Menú",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * In-game banner shown on the playfield immediately when the final orange peg is cleared
 * while the ball drops toward the Extreme Fever score bins.
 */
@Composable
fun ExtremeFeverInGameBanner(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fever_banner")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "banner_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        NeonMagenta.copy(alpha = 0.92f),
                        NeonOrange.copy(alpha = 0.95f),
                        NeonYellow.copy(alpha = 0.92f)
                    )
                )
            )
            .border(2.5.dp, Color.White, RoundedCornerShape(20.dp))
            .padding(horizontal = 28.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "⚡ EXTREME FEVER! ⚡",
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "¡TODOS LOS PEGS NARANJAS ELIMINADOS!",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 0.8.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun DrawScope.drawCelebrationParticle(p: CelebrationParticle) {
    val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
    val color = p.color.copy(alpha = alpha)

    when (p.kind) {
        FeverParticleKind.SHOCKWAVE -> {
            drawCircle(
                color = color.copy(alpha = alpha * 0.7f),
                radius = p.size,
                center = Offset(p.x, p.y),
                style = Stroke(width = (4f * alpha).coerceAtLeast(1.5f))
            )
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.9f),
                radius = (p.size * 0.96f).coerceAtLeast(1f),
                center = Offset(p.x, p.y),
                style = Stroke(width = 1.5f)
            )
        }
        FeverParticleKind.STAR -> {
            rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
                val arm = p.size * 1.5f
                val strokeW = (p.size * 0.35f).coerceAtLeast(1.8f)
                drawLine(
                    color = color,
                    start = Offset(p.x - arm, p.y),
                    end = Offset(p.x + arm, p.y),
                    strokeWidth = strokeW
                )
                drawLine(
                    color = color,
                    start = Offset(p.x, p.y - arm),
                    end = Offset(p.x, p.y + arm),
                    strokeWidth = strokeW
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = p.size * 0.5f,
                    center = Offset(p.x, p.y)
                )
            }
        }
        FeverParticleKind.CONFETTI -> {
            rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
                drawRect(
                    color = color,
                    topLeft = Offset(p.x - p.size / 2f, p.y - p.size / 4f),
                    size = Size(p.size, p.size / 2f)
                )
            }
        }
        FeverParticleKind.SPARK -> {
            // Bright glowing core + halo
            drawCircle(
                color = color.copy(alpha = alpha * 0.45f),
                radius = p.size * 1.8f,
                center = Offset(p.x, p.y)
            )
            drawCircle(
                color = color,
                radius = p.size,
                center = Offset(p.x, p.y)
            )
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = p.size * 0.45f,
                center = Offset(p.x, p.y)
            )
        }
    }
}
