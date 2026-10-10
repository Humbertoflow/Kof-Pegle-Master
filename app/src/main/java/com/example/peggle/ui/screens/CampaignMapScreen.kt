package com.example.peggle.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.peggle.data.LevelCatalog
import com.example.peggle.data.LevelData
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonYellow
import kotlin.math.sin

enum class CampaignMapViewMode {
    CYBER_MAP,
    CHAPTER_GRID
}

enum class LevelStatusFilter {
    ALL,
    CLEARED,
    UPCOMING
}

data class CampaignChapterInfo(
    val chapterIndex: Int,
    val title: String,
    val subtitle: String,
    val stageRange: IntRange,
    val bgRes: Int,
    val themeColor: Color,
    val bossName: String
)

val CAMPAIGN_CHAPTERS = listOf(
    CampaignChapterInfo(
        chapterIndex = 1,
        title = "Capítulo 1: Neo South Town",
        subtitle = "Llamas de Terry Bogard & Geese Tower",
        stageRange = 1..52,
        bgRes = R.drawable.bg_kof_southtown,
        themeColor = NeonOrange,
        bossName = "Terry Bogard"
    ),
    CampaignChapterInfo(
        chapterIndex = 2,
        title = "Capítulo 2: Bosque Sagrado Orochi",
        subtitle = "Pacto de los Tres Tesoros Sagrados",
        stageRange = 53..104,
        bgRes = R.drawable.bg_kof_orochi,
        themeColor = NeonMagenta,
        bossName = "Iori Yagami"
    ),
    CampaignChapterInfo(
        chapterIndex = 3,
        title = "Capítulo 3: Santuario Psico-Cyber",
        subtitle = "Energía Psíquica de Athena Asamiya",
        stageRange = 105..156,
        bgRes = R.drawable.bg_kof_mai,
        themeColor = NeonCyan,
        bossName = "Mai Shiranui"
    ),
    CampaignChapterInfo(
        chapterIndex = 4,
        title = "Capítulo 4: Gran Torneo KOF",
        subtitle = "El Clímax Mundial de los Campeones",
        stageRange = 157..208,
        bgRes = R.drawable.bg_kof_kyo,
        themeColor = NeonYellow,
        bossName = "Kyo Kusanagi"
    )
)

/**
 * Screen displaying the complete 208-level Campaign Map and Progress Tracker.
 * Shows overall completion percentage, cleared vs upcoming nodes, stars earned,
 * interactive winding journey path with glowing checkpoints, and quick level jump.
 */
@Composable
fun CampaignMapScreen(
    onSelectLevel: (LevelData) -> Unit,
    onBack: () -> Unit,
    getHighScore: (String) -> Int,
    getStars: (String) -> Int,
    isCleared: (String) -> Boolean,
    isMasterClear: (String) -> Boolean
) {
    val allLevels = remember { LevelCatalog.getAllLevels() }
    val totalLevels = allLevels.size.coerceAtLeast(208)

    // Compute progress stats
    val clearedCount = remember(allLevels) {
        allLevels.count { isCleared(it.id) }
    }
    val totalStars = remember(allLevels) {
        allLevels.sumOf { getStars(it.id) }
    }
    val maxPossibleStars = totalLevels * 3
    val progressRatio = (clearedCount.toFloat() / totalLevels.toFloat()).coerceIn(0f, 1f)

    // Current unlocked level index (first uncleared level or level 1)
    val currentLevelNumber = remember(allLevels) {
        val firstUncleared = allLevels.firstOrNull { !isCleared(it.id) }
        firstUncleared?.stageNumber ?: totalLevels
    }

    var selectedChapter by remember { mutableIntStateOf(1) }
    var statusFilter by remember { mutableStateOf(LevelStatusFilter.ALL) }
    var viewMode by remember { mutableStateOf(CampaignMapViewMode.CYBER_MAP) }
    var selectedLevelForModal by remember { mutableStateOf<LevelData?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF04040A))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- 1. TOP HEADER & TELEMETRY ---
            CampaignTopHeader(
                clearedCount = clearedCount,
                totalLevels = totalLevels,
                totalStars = totalStars,
                maxPossibleStars = maxPossibleStars,
                progressRatio = progressRatio,
                allLevels = allLevels,
                selectedChapter = selectedChapter,
                onSelectChapter = { selectedChapter = it },
                isCleared = isCleared,
                viewMode = viewMode,
                onToggleViewMode = {
                    viewMode = if (viewMode == CampaignMapViewMode.CYBER_MAP) {
                        CampaignMapViewMode.CHAPTER_GRID
                    } else {
                        CampaignMapViewMode.CYBER_MAP
                    }
                },
                onBack = onBack
            )

            // --- 2. CHAPTER SELECTOR TABS ---
            CampaignChapterTabs(
                selectedChapter = selectedChapter,
                onSelectChapter = { selectedChapter = it },
                allLevels = allLevels,
                isCleared = isCleared
            )

            // Status Filter Chips (Todos / Superados / Próximos)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF060714))
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val unclearedCount = totalLevels - clearedCount
                CampaignFilterChip(
                    selected = statusFilter == LevelStatusFilter.ALL,
                    onClick = { statusFilter = LevelStatusFilter.ALL },
                    label = "Todos ($totalLevels)",
                    themeColor = NeonCyan
                )
                CampaignFilterChip(
                    selected = statusFilter == LevelStatusFilter.CLEARED,
                    onClick = { statusFilter = LevelStatusFilter.CLEARED },
                    label = "Superados ($clearedCount)",
                    themeColor = NeonGreen
                )
                CampaignFilterChip(
                    selected = statusFilter == LevelStatusFilter.UPCOMING,
                    onClick = { statusFilter = LevelStatusFilter.UPCOMING },
                    label = "Próximos ($unclearedCount)",
                    themeColor = NeonOrange
                )
            }

            // --- 3. MAIN CONTENT: CYBER MAP OR CHAPTER GRID ---
            val activeChapterInfo = CAMPAIGN_CHAPTERS.first { it.chapterIndex == selectedChapter }
            val chapterLevels = remember(selectedChapter, allLevels) {
                allLevels.filter { it.stageNumber in activeChapterInfo.stageRange }
            }
            val displayedChapterLevels = remember(chapterLevels, statusFilter) {
                when (statusFilter) {
                    LevelStatusFilter.ALL -> chapterLevels
                    LevelStatusFilter.CLEARED -> chapterLevels.filter { isCleared(it.id) }
                    LevelStatusFilter.UPCOMING -> chapterLevels.filter { !isCleared(it.id) }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (viewMode == CampaignMapViewMode.CYBER_MAP) {
                    WindingCyberPathMap(
                        chapterInfo = activeChapterInfo,
                        levels = chapterLevels,
                        currentActiveStage = currentLevelNumber,
                        pulseGlow = pulseGlow,
                        isCleared = isCleared,
                        isMasterClear = isMasterClear,
                        getStars = getStars,
                        getHighScore = getHighScore,
                        onNodeClick = { level ->
                            selectedLevelForModal = level
                        }
                    )
                } else {
                    ChapterLevelGrid(
                        levels = displayedChapterLevels,
                        currentActiveStage = currentLevelNumber,
                        isCleared = isCleared,
                        isMasterClear = isMasterClear,
                        getStars = getStars,
                        getHighScore = getHighScore,
                        onSelectLevel = { level ->
                            selectedLevelForModal = level
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }

        // Floating Quick Continue Button
        val nextUpcomingLevel = remember(currentLevelNumber, allLevels) {
            allLevels.find { it.stageNumber == currentLevelNumber } ?: allLevels.first()
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Button(
                onClick = { onSelectLevel(nextUpcomingLevel) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .shadow(8.dp, RoundedCornerShape(14.dp))
                    .testTag("campaign_quick_continue_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CONTINUAR: NIVEL ${nextUpcomingLevel.stageNumber} • ${nextUpcomingLevel.title}",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
            }
        }

        // --- 4. LEVEL DETAILS MODAL DIALOG ON NODE CLICK ---
        selectedLevelForModal?.let { level ->
            val isLocked = level.stageNumber > currentLevelNumber && !isCleared(level.id)
            val cleared = isCleared(level.id)
            val stars = getStars(level.id)
            val hs = getHighScore(level.id)

            LevelNodeModalSheet(
                level = level,
                isLocked = isLocked,
                isCleared = cleared,
                stars = stars,
                highScore = hs,
                onDismiss = { selectedLevelForModal = null },
                onPlay = {
                    val target = selectedLevelForModal ?: level
                    selectedLevelForModal = null
                    onSelectLevel(target)
                }
            )
        }
    }
}

/**
 * Top App Header showing 208-level progress bar and stats counter.
 */
@Composable
private fun CampaignTopHeader(
    clearedCount: Int,
    totalLevels: Int,
    totalStars: Int,
    maxPossibleStars: Int,
    progressRatio: Float,
    allLevels: List<LevelData>,
    selectedChapter: Int,
    onSelectChapter: (Int) -> Unit,
    isCleared: (String) -> Boolean,
    viewMode: CampaignMapViewMode,
    onToggleViewMode: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0C0E1E),
                        Color(0xFF060712)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(NeonCyan.copy(alpha = 0.5f), NeonMagenta.copy(alpha = 0.5f))
                ),
                shape = RectangleShape
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("campaign_map_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = NeonCyan
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = NeonYellow,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MAPA DE CAMPAÑA 208",
                            color = NeonYellow,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                    }
                    Text(
                        text = "Progreso Global: $clearedCount / $totalLevels Tableros (${(progressRatio * 100).toInt()}%)",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Mode switch button (Mapa Interactivo vs Cuadrícula)
            IconButton(
                onClick = onToggleViewMode,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF13172E))
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .testTag("campaign_view_mode_toggle")
            ) {
                Icon(
                    imageVector = if (viewMode == CampaignMapViewMode.CYBER_MAP) Icons.AutoMirrored.Filled.ViewList else Icons.Default.Map,
                    contentDescription = "Cambiar vista",
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Progress Bar with Neon Cyan/Gold Glow
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$clearedCount superados",
                        color = NeonGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = NeonYellow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$totalStars / $maxPossibleStars estrellas",
                        color = NeonYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${totalLevels - clearedCount} pendientes",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Neon Progress Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFF14182E))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progressRatio)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF00E5FF),
                                    Color(0xFF00FF66),
                                    Color(0xFFFFEA00)
                                )
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4 Chapter Mini Progress Bars (52 levels each)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CAMPAIGN_CHAPTERS.forEach { chap ->
                val chapLevels = allLevels.filter { it.stageNumber in chap.stageRange }
                val chapCleared = chapLevels.count { isCleared(it.id) }
                val chapRatio = (chapCleared.toFloat() / chapLevels.size.toFloat()).coerceIn(0f, 1f)
                val isSelected = selectedChapter == chap.chapterIndex

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF151936) else Color(0xFF090C1F))
                        .border(
                            width = if (isSelected) 1.2.dp else 0.8.dp,
                            color = if (isSelected) chap.themeColor else Color(0xFF1E2442),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectChapter(chap.chapterIndex) }
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Cap. ${chap.chapterIndex}",
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(Color(0xFF1A2040))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(chapRatio)
                                    .height(3.dp)
                                    .background(chap.themeColor)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$chapCleared/52",
                            color = if (chapCleared == 52) NeonGreen else chap.themeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend: Status meaning
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CampaignLegendPill(color = NeonGreen, text = "Superado")
            CampaignLegendPill(color = NeonYellow, text = "Actual")
            CampaignLegendPill(color = NeonOrange, text = "Próximo")
            CampaignLegendPill(color = Color(0xFF64748B), text = "Bloqueado")
            CampaignLegendPill(color = Color(0xFFFFD700), text = "👑 Jefe KOF")
        }
    }
}

@Composable
private fun CampaignLegendPill(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = Color(0xFFCBD5E1),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CampaignFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    themeColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) themeColor.copy(alpha = 0.2f) else Color(0xFF0F1226))
            .border(
                width = if (selected) 1.2.dp else 0.8.dp,
                color = if (selected) themeColor else Color(0xFF1E2442),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = if (selected) themeColor else Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold
        )
    }
}

/**
 * Chapter navigation tabs allowing quick filtering across the 4 major saga chapters.
 */
@Composable
private fun CampaignChapterTabs(
    selectedChapter: Int,
    onSelectChapter: (Int) -> Unit,
    allLevels: List<LevelData>,
    isCleared: (String) -> Boolean
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF060714))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(CAMPAIGN_CHAPTERS, key = { it.chapterIndex }) { chapter ->
            val isSelected = selectedChapter == chapter.chapterIndex
            val chapterLevels = allLevels.filter { it.stageNumber in chapter.stageRange }
            val chapterCleared = chapterLevels.count { isCleared(it.id) }
            val chapterTotal = chapterLevels.size

            Card(
                modifier = Modifier
                    .clickable { onSelectChapter(chapter.chapterIndex) }
                    .testTag("chapter_tab_${chapter.chapterIndex}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF151936) else Color(0xFF0A0D1F)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) chapter.themeColor else Color(0xFF1E2442)
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) chapter.themeColor else Color(0xFF475569))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cap. ${chapter.chapterIndex}",
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = "${chapter.stageRange.first}-${chapter.stageRange.last} ($chapterCleared/$chapterTotal)",
                        color = if (isSelected) chapter.themeColor else Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Winding cybernetic board roadmap with 52 stages per chapter connected by circuits.
 */
@Composable
private fun WindingCyberPathMap(
    chapterInfo: CampaignChapterInfo,
    levels: List<LevelData>,
    currentActiveStage: Int,
    pulseGlow: Float,
    isCleared: (String) -> Boolean,
    isMasterClear: (String) -> Boolean,
    getStars: (String) -> Int,
    getHighScore: (String) -> Int,
    onNodeClick: (LevelData) -> Unit
) {
    val scrollState = rememberScrollState()

    // Auto-scroll to near active level within chapter
    LaunchedEffect(chapterInfo.chapterIndex, levels) {
        val activeIndexInChapter = levels.indexOfFirst { it.stageNumber == currentActiveStage }
        if (activeIndexInChapter != -1) {
            // Approx row height = 90dp -> Scroll smoothly
            val targetScroll = (activeIndexInChapter / 3) * 240
            scrollState.animateScrollTo(targetScroll.coerceAtLeast(0))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // High-tech chapter backdrop
        Image(
            painter = painterResource(id = chapterInfo.bgRes),
            contentDescription = chapterInfo.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .matchParentSize()
        )

        // Dark cyber overlay scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF04040E).copy(alpha = 0.88f),
                            Color(0xFF070718).copy(alpha = 0.85f),
                            Color(0xFF04040E).copy(alpha = 0.94f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Chapter banner card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0D1F).copy(alpha = 0.9f)),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, chapterInfo.themeColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = chapterInfo.title,
                            color = chapterInfo.themeColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        Text(
                            text = chapterInfo.subtitle,
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(chapterInfo.themeColor.copy(alpha = 0.2f))
                            .border(1.dp, chapterInfo.themeColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "JEFE: ${chapterInfo.bossName}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Group levels into rows of 3 in a winding snake layout (Left-to-Right, Right-to-Left)
            val chunked = remember(levels) { levels.chunked(3) }

            chunked.forEachIndexed { rowIndex, rowLevels ->
                val isReversed = rowIndex % 2 == 1
                val orderedLevels = if (isReversed) rowLevels.reversed() else rowLevels

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    orderedLevels.forEach { level ->
                        val cleared = isCleared(level.id)
                        val isMaster = isMasterClear(level.id)
                        val stars = getStars(level.id)
                        val isCurrent = level.stageNumber == currentActiveStage
                        val isLocked = level.stageNumber > currentActiveStage && !cleared

                        CampaignNodeButton(
                            level = level,
                            isCleared = cleared,
                            isMaster = isMaster,
                            stars = stars,
                            isCurrent = isCurrent,
                            isLocked = isLocked,
                            pulseGlow = pulseGlow,
                            themeColor = chapterInfo.themeColor,
                            onClick = { onNodeClick(level) }
                        )
                    }
                }

                // Connecting circuit divider
                if (rowIndex < chunked.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp),
                        contentAlignment = if (isReversed) Alignment.CenterStart else Alignment.CenterEnd
                    ) {
                        // Curved circuit trace to next row
                        Canvas(
                            modifier = Modifier
                                .width(90.dp)
                                .height(18.dp)
                                .padding(horizontal = 24.dp)
                        ) {
                            drawLine(
                                color = chapterInfo.themeColor.copy(alpha = 0.5f),
                                start = Offset(size.width / 2f, 0f),
                                end = Offset(size.width / 2f, size.height),
                                strokeWidth = 3f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Single node in the cybernetic campaign map.
 */
@Composable
private fun CampaignNodeButton(
    level: LevelData,
    isCleared: Boolean,
    isMaster: Boolean,
    stars: Int,
    isCurrent: Boolean,
    isLocked: Boolean,
    pulseGlow: Float,
    themeColor: Color,
    onClick: () -> Unit
) {
    val isBoss = level.stageNumber in listOf(52, 104, 156, 208)
    val isUpcoming = !isCleared && !isLocked && !isCurrent

    val nodeBg = when {
        isMaster -> NeonGreen.copy(alpha = 0.35f)
        isCleared -> Color(0xFF00E5FF).copy(alpha = 0.25f)
        isCurrent -> themeColor.copy(alpha = 0.40f * pulseGlow)
        isUpcoming -> NeonOrange.copy(alpha = 0.22f)
        else -> Color(0xFF10132A)
    }

    val borderColor = when {
        isMaster -> NeonGreen
        isCleared -> NeonCyan
        isCurrent -> NeonYellow
        isUpcoming -> NeonOrange
        else -> Color(0xFF242A4A)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("campaign_node_${level.stageNumber}")
    ) {
        // Status Badge Pill
        val statusTag = when {
            isBoss -> "👑 JEFE"
            isCurrent -> "⚡ ACTUAL"
            isCleared -> "LISTO"
            isUpcoming -> "PRÓXIMO"
            else -> "BLOQ."
        }
        val tagBg = when {
            isBoss -> Color(0xFFFFD700)
            isCurrent -> NeonYellow
            isCleared -> NeonGreen.copy(alpha = 0.25f)
            isUpcoming -> NeonOrange.copy(alpha = 0.25f)
            else -> Color(0xFF14182E)
        }
        val tagTextColor = when {
            isBoss || isCurrent -> Color.Black
            isCleared -> NeonGreen
            isUpcoming -> NeonOrange
            else -> Color(0xFF64748B)
        }

        Box(
            modifier = Modifier
                .padding(bottom = 3.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(tagBg)
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = statusTag,
                color = tagTextColor,
                fontWeight = FontWeight.Black,
                fontSize = 7.5.sp
            )
        }

        Box(
            modifier = Modifier
                .size(if (isCurrent) 64.dp else 56.dp)
                .clip(CircleShape)
                .background(nodeBg)
                .border(
                    width = if (isCurrent) 2.5.dp else 1.8.dp,
                    color = borderColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLocked -> {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Bloqueado",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
                isCurrent -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${level.stageNumber}",
                            color = NeonYellow,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Actual",
                            tint = NeonYellow,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                isCleared -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${level.stageNumber}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Superado",
                            tint = if (isMaster) NeonGreen else NeonCyan,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                else -> {
                    Text(
                        text = "${level.stageNumber}",
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Star indicator row
        Row(horizontalArrangement = Arrangement.Center) {
            for (i in 1..3) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (i <= stars) NeonYellow else Color(0xFF1E2442),
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

/**
 * Grid view alternative allowing quick browsing of stages in rows with full metadata.
 */
@Composable
private fun ChapterLevelGrid(
    levels: List<LevelData>,
    currentActiveStage: Int,
    isCleared: (String) -> Boolean,
    isMasterClear: (String) -> Boolean,
    getStars: (String) -> Int,
    getHighScore: (String) -> Int,
    onSelectLevel: (LevelData) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(levels, key = { it.id }) { level ->
            val cleared = isCleared(level.id)
            val master = isMasterClear(level.id)
            val stars = getStars(level.id)
            val hs = getHighScore(level.id)
            val isLocked = level.stageNumber > currentActiveStage && !cleared

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectLevel(level) }
                    .testTag("campaign_grid_item_${level.stageNumber}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (cleared) Color(0xFF0F1226) else Color(0xFF070914)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (master) 2.dp else if (cleared) 1.5.dp else 1.dp,
                    color = if (master) NeonGreen else if (cleared) NeonCyan else Color(0xFF1E2442)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, NeonCyan, RoundedCornerShape(10.dp))
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
                                .background(Color.Black.copy(alpha = 0.35f))
                        )
                        Text(
                            text = "#${level.stageNumber}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = level.title,
                            color = if (isLocked) Color(0xFF64748B) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${level.master.title} • ${level.subtitle}",
                            color = Color(0xFF8B9CB5),
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                        if (hs > 0) {
                            Text(
                                text = "Récord: ${String.format("%,d", hs)} pts",
                                color = NeonYellow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        if (isLocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Bloqueado",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Row {
                                for (i in 1..3) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (i <= stars) NeonYellow else Color(0xFF1E2442),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            if (cleared) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (master) "MASTER" else "LISTO",
                                    color = if (master) NeonGreen else NeonCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Bottom tactical modal sheet to inspect level details and launch.
 */
@Composable
private fun LevelNodeModalSheet(
    level: LevelData,
    isLocked: Boolean,
    isCleared: Boolean,
    stars: Int,
    highScore: Int,
    onDismiss: () -> Unit,
    onPlay: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("level_node_modal"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF090B1E)),
            border = androidx.compose.foundation.BorderStroke(1.8.dp, NeonCyan)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header thumbnail
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.2.dp, NeonCyan, RoundedCornerShape(16.dp))
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
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xFF04040E).copy(alpha = 0.9f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "NIVEL ${level.stageNumber}",
                            color = NeonYellow,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                        Text(
                            text = level.title,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Subtitle & Master
                Text(
                    text = level.subtitle,
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Stats row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF10142A))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ESTRELLAS", color = Color(0xFF8B9CB5), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row {
                            for (i in 1..3) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (i <= stars) NeonYellow else Color(0xFF1E2442),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("RÉCORD", color = Color(0xFF8B9CB5), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (highScore > 0) String.format("%,d", highScore) else "--",
                            color = NeonCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ESTADO", color = Color(0xFF8B9CB5), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isCleared) "SUPERADO" else "PENDIENTE",
                            color = if (isCleared) NeonGreen else NeonOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action button
                Button(
                    onClick = onPlay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("launch_campaign_level_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLocked) Color(0xFF1E2442) else NeonOrange
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isLocked) "DESAFIAR NIVEL" else "JUGAR NIVEL",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
