package com.example.peggle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peggle.model.PeggleMaster
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonYellow

@Composable
fun ScoreHeader(
    title: String,
    score: Int,
    highScore: Int,
    ballsRemaining: Int,
    master: PeggleMaster,
    soundEnabled: Boolean,
    powerMessage: String?,
    onToggleSound: () -> Unit,
    onPause: () -> Unit,
    onToggleOrientation: (() -> Unit)? = null,
    onOpenMap: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Master Avatar + Level Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(master.accentColor))
                        .border(1.8.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = master.emoji,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Maestro: ${master.title}",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Digital HUD Score Counter
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%,d", score),
                    color = NeonYellow,
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp
                )
                Text(
                    text = "RÉCORD: ${String.format("%,d", highScore)}",
                    color = Color(0xFF8B9CB5),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Map, Screen Rotation, Sound, and Pause buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onOpenMap != null) {
                    IconButton(
                        onClick = onOpenMap,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("map_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Mapa de Campaña",
                            tint = NeonYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (onToggleOrientation != null) {
                    IconButton(
                        onClick = onToggleOrientation,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("rotate_screen_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenRotation,
                            contentDescription = "Rotar Pantalla",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("sound_toggle_button")
                ) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Sonido",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onPause,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pausa",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Row of 3D Neon Ball Tokens Remaining
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ESFERAS: ",
                    color = Color(0xFF8B9CB5),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )

                val displayCount = ballsRemaining.coerceAtMost(10)
                for (i in 0 until displayCount) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color.White, Color(0xFF00E5FF), Color(0xFF003B5C))
                                )
                            )
                            .border(1.dp, NeonCyan, CircleShape)
                    )
                }
                if (ballsRemaining > 10) {
                    Text(
                        text = "+${ballsRemaining - 10}",
                        color = NeonYellow,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }

            // Power message HUD pill
            if (powerMessage != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(master.accentColor).copy(alpha = 0.85f))
                        .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = powerMessage,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
