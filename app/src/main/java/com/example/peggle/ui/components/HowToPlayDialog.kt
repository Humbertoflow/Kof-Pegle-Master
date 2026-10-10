package com.example.peggle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonYellow

@Composable
fun HowToPlayDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("how_to_play_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF070714)),
            border = androidx.compose.foundation.BorderStroke(1.8.dp, NeonCyan)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MANUAL TÁCTICO 3D NEON",
                    color = NeonYellow,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Section 1: Objective
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0E1026))
                        .border(1.dp, Color(0xFF1E2442), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "🎯 Objetivo: 25 Esferas Naranjas",
                        color = NeonOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dispara desde el cañón superior para destruir todas las 25 esferas naranja. Al limpiar la última, entrarás en EXTREME FEVER al compás del Himno a la Alegría.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section 2: Neon Peg Types
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0E1026))
                        .border(1.dp, Color(0xFF1E2442), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "💎 Tipos de Esferas Neon",
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    NeonPegRow(color = NeonOrange, title = "Naranja (25 objetivos)", desc = "Aumenta el multiplicador de fiebre y desbloquea Extreme Fever.")
                    Spacer(modifier = Modifier.height(6.dp))
                    NeonPegRow(color = NeonCyan, title = "Cian Eléctrico", desc = "Esferas base que aportan puntos de combo.")
                    Spacer(modifier = Modifier.height(6.dp))
                    NeonPegRow(color = NeonGreen, title = "Verde Radiactivo (2 mágicas)", desc = "Activa el superpoder único del Maestro Peggle.")
                    Spacer(modifier = Modifier.height(6.dp))
                    NeonPegRow(color = NeonMagenta, title = "Magenta Cyber (Rotativa)", desc = "Cambia de posición cada tiro: ¡otorga 500x de bonificación!")
                    Spacer(modifier = Modifier.height(6.dp))
                    NeonPegRow(color = Color(0xFF00E5FF), title = "Esfera Multibola (Doble Núcleo)", desc = "¡Divide la esfera en 2 bolas activas simultáneas multiplicando los impactos!")
                    Spacer(modifier = Modifier.height(6.dp))
                    NeonPegRow(color = NeonYellow, title = "Esfera Puntería Extendida (Long Aim)", desc = "Extiende la mira láser táctica y predice hasta 4 rebotes consecutivos.")
                    Spacer(modifier = Modifier.height(6.dp))
                    NeonPegRow(color = Color(0xFFFF3B30), title = "Esfera Onda Explosiva (Blast Radius)", desc = "Detona una explosión de plasma que destruye las esferas del vecindario.")
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section 3: Hover Bucket
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0E1026))
                        .border(1.dp, Color(0xFF1E2442), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "🧺 Cubo Deslizante Holográfico",
                        color = NeonYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "El receptor inferior patrulla el fondo. Si la esfera cae dentro, ¡ganas una ESFERA GRATIS! También se conceden esferas extra al superar 25,000 pts en un solo tiro.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("dismiss_guide_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)
                ) {
                    Text(text = "¡Iniciar Desafío!", color = Color.White, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun NeonPegRow(color: Color, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(14.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, Color.White, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(text = desc, color = Color(0xFF8B9CB5), fontSize = 11.sp)
        }
    }
}
