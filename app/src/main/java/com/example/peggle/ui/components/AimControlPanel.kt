package com.example.peggle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peggle.model.ShotPhase
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonOrange

@Composable
fun AimControlPanel(
    cannonAngleDeg: Float,
    ballsRemaining: Int,
    shotPhase: ShotPhase,
    isFastForward: Boolean,
    onAngleChange: (Float) -> Unit,
    onNudgeAngle: (Float) -> Unit,
    onFire: () -> Unit,
    onToggleFastForward: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color(0xFF070714).copy(alpha = 0.96f))
            .border(
                width = 1.5.dp,
                color = NeonCyan.copy(alpha = 0.5f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Nudge Left (-1.5°)
            FilledIconButton(
                onClick = { onNudgeAngle(-1.5f) },
                enabled = shotPhase == ShotPhase.AIMING,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("nudge_left_button"),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF101326),
                    contentColor = NeonCyan
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Ajustar izquierda -1.5°"
                )
            }

            // Central Laser Aim Slider
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Slider(
                    value = cannonAngleDeg,
                    onValueChange = onAngleChange,
                    valueRange = 18f..162f,
                    enabled = shotPhase == ShotPhase.AIMING,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("aim_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = Color(0xFF1E2442)
                    )
                )
            }

            // Nudge Right (+1.5°)
            FilledIconButton(
                onClick = { onNudgeAngle(1.5f) },
                enabled = shotPhase == ShotPhase.AIMING,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("nudge_right_button"),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF101326),
                    contentColor = NeonCyan
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Ajustar derecha +1.5°"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Fast forward toggle (when ball is active)
            if (shotPhase == ShotPhase.BALL_IN_PLAY) {
                FilledIconButton(
                    onClick = onToggleFastForward,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("fast_forward_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isFastForward) NeonOrange else Color(0xFF101326),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Acelerar 2X"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // NEON FIRE BUTTON
            Button(
                onClick = onFire,
                enabled = shotPhase == ShotPhase.AIMING && ballsRemaining > 0,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("fire_button"),
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
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "¡FUEGO!",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
