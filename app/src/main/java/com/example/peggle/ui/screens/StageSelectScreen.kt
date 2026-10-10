package com.example.peggle.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peggle.data.LevelCatalog
import com.example.peggle.data.LevelData
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonYellow

@Composable
fun StageSelectScreen(
    onSelectLevel: (LevelData) -> Unit,
    onBack: () -> Unit,
    onOpenMap: (() -> Unit)? = null,
    onOpenEditor: (() -> Unit)? = null,
    getHighScore: (String) -> Int,
    getStars: (String) -> Int,
    isCleared: (String) -> Boolean,
    isMasterClear: (String) -> Boolean
) {
    val levels = LevelCatalog.getAllLevels()
    val totalLevels = levels.size.coerceAtLeast(208)
    val clearedCount = remember(levels) { levels.count { isCleared(it.id) } }
    val progressPercent = (clearedCount.toFloat() / totalLevels.toFloat() * 100).toInt()

    var selectedChapter by remember { mutableIntStateOf(0) }
    val chapterFilters = listOf(
        "Todos (${levels.size})",
        "Cap. 1: 1-52",
        "Cap. 2: 53-104",
        "Cap. 3: 105-156",
        "Cap. 4: 157-208"
    )
    val displayedLevels = remember(selectedChapter, levels) {
        when (selectedChapter) {
            1 -> levels.filter { it.stageNumber in 1..52 }
            2 -> levels.filter { it.stageNumber in 53..104 }
            3 -> levels.filter { it.stageNumber in 105..156 }
            4 -> levels.filter { it.stageNumber in 157..208 }
            else -> levels
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF04040B))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("stage_select_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = NeonCyan
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "SECTOR DE MISIONES 3D",
                        color = NeonYellow,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "$clearedCount / $totalLevels superados • $progressPercent% de Campaña",
                        color = Color(0xFF8B9CB5),
                        fontSize = 12.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onOpenMap != null) {
                    IconButton(
                        onClick = onOpenMap,
                        modifier = Modifier.testTag("stage_select_map_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Mapa de Campaña 208",
                            tint = NeonCyan
                        )
                    }
                }

                if (onOpenEditor != null) {
                    IconButton(
                        onClick = onOpenEditor,
                        modifier = Modifier.testTag("stage_select_editor_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editor de Niveles",
                            tint = NeonYellow
                        )
                    }
                }
            }
        }

        // Quick Map Banner & Progress Tracker Card
        if (onOpenMap != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .clickable { onOpenMap() }
                    .testTag("open_campaign_map_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F132E)),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, NeonCyan.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCyan.copy(alpha = 0.2f))
                                .border(1.dp, NeonCyan, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "VER MAPA MUNDIAL DE CAMPAÑA",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Circuito de 208 nodos con estatus de bloqueo y jefes",
                                color = NeonYellow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$progressPercent%",
                            color = NeonGreen,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Chapter Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chapterFilters.size) { idx ->
                val isSelected = selectedChapter == idx
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) NeonCyan.copy(alpha = 0.25f) else Color(0xFF10142A))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) NeonCyan else Color(0xFF222B52),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedChapter = idx }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = chapterFilters[idx],
                        color = if (isSelected) NeonCyan else Color(0xFF8B9CB5),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(displayedLevels, key = { it.id }) { level ->
                val hs = getHighScore(level.id)
                val stars = getStars(level.id)
                val cleared = isCleared(level.id)
                val masterClear = isMasterClear(level.id)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("level_card_${level.id}")
                        .clickable { onSelectLevel(level) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (cleared) Color(0xFF0F1226) else Color(0xFF080914)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (masterClear) 2.dp else if (cleared) 1.5.dp else 1.dp,
                        color = if (masterClear) NeonGreen else if (cleared) NeonCyan else Color(0xFF1E2442)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // KOF Arena Stage Thumbnail with glowing rim
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, NeonCyan, RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                painter = painterResource(id = level.bgDrawableRes),
                                contentDescription = level.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.25f))
                            )
                            Text(
                                text = level.master.emoji,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Level details
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Nivel ${level.stageNumber}: ${level.title}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            Text(
                                text = level.subtitle,
                                color = Color(0xFF8B9CB5),
                                fontSize = 12.sp,
                                maxLines = 1
                            )

                            if (hs > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Récord: ${String.format("%,d", hs)} pts",
                                    color = NeonYellow,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Stars or clear badge
                        Column(horizontalAlignment = Alignment.End) {
                            Row {
                                for (i in 1..3) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (i <= stars) NeonYellow else Color(0xFF1E2442),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            if (masterClear) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonGreen.copy(alpha = 0.2f))
                                        .border(1.dp, NeonGreen, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "100%",
                                        color = NeonGreen,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
