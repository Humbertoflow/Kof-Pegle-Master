package com.example.peggle.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonYellow

@Composable
fun FeverMeter(
    orangePegsRemaining: Int,
    totalOrangePegs: Int,
    currentMultiplier: Int,
    modifier: Modifier = Modifier
) {
    val progress = (1f - (orangePegsRemaining.toFloat() / totalOrangePegs.coerceAtLeast(1))).coerceIn(0f, 1f)

    val multiplierNeonColor by animateColorAsState(
        targetValue = when (currentMultiplier) {
            10 -> NeonMagenta
            5 -> NeonOrange
            3 -> NeonYellow
            2 -> NeonGreen
            else -> NeonCyan
        },
        label = "multiplierNeonColor"
    )

    Column(
        modifier = modifier
            .testTag("fever_meter")
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF070714).copy(alpha = 0.92f))
            .border(1.5.dp, multiplierNeonColor.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp, horizontal = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Multiplier Neon Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(multiplierNeonColor)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "${currentMultiplier}X",
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Neon Plasma Tube Track
        Box(
            modifier = Modifier
                .width(12.dp)
                .height(130.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0F1024))
                .border(1.dp, Color(0xFF1E2442), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Neon plasma column
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .height((130 * progress).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                multiplierNeonColor,
                                NeonOrange,
                                NeonCyan
                            )
                        )
                    )
            )

            // Inner core laser filament
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height((130 * progress).dp)
                    .background(Color.White.copy(alpha = 0.8f))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Orange peg count badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(NeonOrange)
                    .border(1.dp, Color.White, CircleShape)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "$orangePegsRemaining",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}
