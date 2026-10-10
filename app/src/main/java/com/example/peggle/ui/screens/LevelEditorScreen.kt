package com.example.peggle.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.peggle.data.LevelData
import com.example.peggle.data.LevelManager
import com.example.peggle.data.PegTemplate
import com.example.peggle.data.local.LevelConfigEntity
import com.example.peggle.model.PegShape
import com.example.peggle.model.PegType
import com.example.peggle.model.PeggleMaster
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonYellow
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

enum class EditorToolMode {
    ADD,
    SELECT,
    ERASE
}

data class BackgroundOption(
    val name: String,
    val resId: Int,
    val gradient: Pair<Long, Long>
)

val availableBackgrounds = listOf(
    BackgroundOption("Kyo Fuego", R.drawable.bg_kof_kyo, Pair(0xFF132B47L, 0xFF19402AL)),
    BackgroundOption("South Town", R.drawable.bg_kof_southtown, Pair(0xFF1B2A4AL, 0xFF2B4D3FL)),
    BackgroundOption("Mai Shiranui", R.drawable.bg_kof_mai, Pair(0xFF2B1238L, 0xFF4A1E14L)),
    BackgroundOption("Iori Luna", R.drawable.bg_kof_iori, Pair(0xFF1E1128L, 0xFF3D1630L)),
    BackgroundOption("Orochi Sello", R.drawable.bg_kof_orochi, Pair(0xFF28133EL, 0xFF431F6BL)),
    BackgroundOption("Bosque Anime", R.drawable.bg_anime_forest, Pair(0xFF0F261EL, 0xFF183D2AL)),
    BackgroundOption("Cosmos", R.drawable.bg_anime_cosmic, Pair(0xFF0C1636L, 0xFF1F225CL))
)

@Composable
fun LevelEditorScreen(
    levelManager: LevelManager,
    onBack: () -> Unit,
    onPlayCustomLevel: (LevelData) -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Editor state
    val pegsList = remember { mutableStateListOf<PegTemplate>() }
    var selectedIndex by remember { mutableIntStateOf(-1) }
    var toolMode by remember { mutableStateOf(EditorToolMode.ADD) }
    var activeShape by remember { mutableStateOf(PegShape.CIRCLE) }
    var activeType by remember { mutableStateOf(PegType.BLUE) }
    var activeRotation by remember { mutableFloatStateOf(0f) }
    var snapToGrid by remember { mutableStateOf(true) }

    // Level settings
    var levelTitle by remember { mutableStateOf("Mi Arena KOF") }
    var levelSubtitle by remember { mutableStateOf("Tablero táctico personalizado") }
    var selectedMaster by remember { mutableStateOf(PeggleMaster.BJORN) }
    var selectedBackground by remember { mutableStateOf(availableBackgrounds.first()) }
    var targetOrange by remember { mutableIntStateOf(20) }
    var targetGreen by remember { mutableIntStateOf(2) }
    var initialBalls by remember { mutableIntStateOf(10) }

    var showConfigDialog by remember { mutableStateOf(false) }
    var showPresetsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060610))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // --- TOP BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("editor_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = NeonCyan
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "EDITOR DE NIVELES",
                        color = NeonYellow,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${pegsList.size} clavijas • Room DB",
                        color = Color(0xFF8B9CB5),
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Presets Button
                IconButton(
                    onClick = { showPresetsDialog = true },
                    modifier = Modifier.testTag("editor_presets_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Plantillas",
                        tint = NeonMagenta
                    )
                }

                // Settings Button
                IconButton(
                    onClick = { showConfigDialog = true },
                    modifier = Modifier.testTag("editor_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ajustes del Nivel",
                        tint = NeonCyan
                    )
                }

                // Save to Room Button
                Button(
                    onClick = {
                        if (pegsList.size < 10) {
                            Toast.makeText(context, "Coloca al menos 10 clavijas antes de guardar", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        coroutineScope.launch {
                            val customId = "custom_${System.currentTimeMillis()}"
                            val customLevel = LevelConfigEntity(
                                id = customId,
                                stageNumber = 99,
                                title = levelTitle,
                                subtitle = levelSubtitle,
                                masterName = selectedMaster.name,
                                bgGradientStart = selectedBackground.gradient.first,
                                bgGradientEnd = selectedBackground.gradient.second,
                                bgDrawableRes = selectedBackground.resId,
                                targetOrangePegs = targetOrange.coerceAtMost(pegsList.size - 2),
                                targetGreenPegs = targetGreen.coerceAtMost(2),
                                targetPurplePegs = 1,
                                initialBallCount = initialBalls,
                                parScore = 120_000,
                                pegLayoutJson = com.example.peggle.data.local.PegLayoutSerializer.serialize(pegsList)
                            )
                            levelManager.saveLevel(customLevel)
                            Toast.makeText(context, "¡Nivel guardado en la base de datos Room!", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("editor_save_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Guardar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Play / Test Button
                Button(
                    onClick = {
                        if (pegsList.size < 10) {
                            Toast.makeText(context, "Coloca al menos 10 clavijas para probar", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val levelData = LevelData(
                            id = "custom_test",
                            stageNumber = 99,
                            title = levelTitle,
                            subtitle = levelSubtitle,
                            master = selectedMaster,
                            backgroundGradient = selectedBackground.gradient,
                            bgDrawableRes = selectedBackground.resId,
                            templates = pegsList.toList()
                        )
                        onPlayCustomLevel(levelData)
                    },
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("editor_play_test_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Jugar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // --- SUB-HEADER STATS & GRID TOGGLE ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatsBadge(label = "Clavijas", value = "${pegsList.size}", color = NeonCyan)
                StatsBadge(label = "Naranjas", value = "$targetOrange", color = NeonOrange)
                StatsBadge(label = "Bolas", value = "$initialBalls", color = NeonYellow)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { snapToGrid = !snapToGrid },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = "Cuadrícula",
                        tint = if (snapToGrid) NeonCyan else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = {
                        pegsList.clear()
                        selectedIndex = -1
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Limpiar Todo",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // --- INTERACTIVE CANVAS ---
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(1.5.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
        ) {
            val canvasWidth = constraints.maxWidth.toFloat()
            val canvasHeight = constraints.maxHeight.toFloat()

            // Background Image
            Image(
                painter = painterResource(id = selectedBackground.resId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dim overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            )

            // Drawing canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("editor_canvas")
                    .pointerInput(toolMode, activeShape, activeType, activeRotation, snapToGrid) {
                        detectTapGestures { tapOffset ->
                            val rawXRatio = tapOffset.x / canvasWidth
                            val rawYRatio = tapOffset.y / canvasHeight

                            // Keep within playable bounds (below cannon, above bucket)
                            if (rawYRatio !in 0.22f..0.85f || rawXRatio !in 0.08f..0.92f) {
                                return@detectTapGestures
                            }

                            // Check if tapped near an existing peg
                            val tapRadius = 24f
                            val existingIndex = pegsList.indexOfFirst { peg ->
                                val px = peg.xRatio * canvasWidth
                                val py = peg.yRatio * canvasHeight
                                hypot(tapOffset.x - px, tapOffset.y - py) <= tapRadius
                            }

                            when (toolMode) {
                                EditorToolMode.ERASE -> {
                                    if (existingIndex != -1) {
                                        pegsList.removeAt(existingIndex)
                                        if (selectedIndex == existingIndex) selectedIndex = -1
                                    }
                                }
                                EditorToolMode.SELECT -> {
                                    selectedIndex = existingIndex
                                }
                                EditorToolMode.ADD -> {
                                    if (existingIndex != -1) {
                                        // Tap on existing peg selects it
                                        selectedIndex = existingIndex
                                    } else {
                                        // Place new peg
                                        var xRatio = rawXRatio
                                        var yRatio = rawYRatio
                                        if (snapToGrid) {
                                            val stepX = 0.04f
                                            val stepY = 0.04f
                                            xRatio = (rawXRatio / stepX).roundToInt() * stepX
                                            yRatio = (rawYRatio / stepY).roundToInt() * stepY
                                        }

                                        val newPeg = PegTemplate(
                                            xRatio = xRatio.coerceIn(0.10f, 0.90f),
                                            yRatio = yRatio.coerceIn(0.24f, 0.82f),
                                            defaultType = activeType,
                                            shape = activeShape,
                                            width = if (activeShape == PegShape.BRICK) 44f else 15f,
                                            height = if (activeShape == PegShape.BRICK) 14f else 15f,
                                            rotation = activeRotation
                                        )
                                        pegsList.add(newPeg)
                                        selectedIndex = pegsList.size - 1
                                    }
                                }
                            }
                        }
                    }
            ) {
                // Grid lines if enabled
                if (snapToGrid) {
                    val cols = 20
                    val rows = 20
                    for (c in 1 until cols) {
                        val gx = c * (size.width / cols)
                        drawLine(Color.White.copy(alpha = 0.06f), Offset(gx, 0f), Offset(gx, size.height), strokeWidth = 1f)
                    }
                    for (r in 1 until rows) {
                        val gy = r * (size.height / rows)
                        drawLine(Color.White.copy(alpha = 0.06f), Offset(0f, gy), Offset(size.width, gy), strokeWidth = 1f)
                    }
                }

                // Top Cannon Zone guide
                drawRect(
                    color = Color.Black.copy(alpha = 0.4f),
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, size.height * 0.20f)
                )
                drawLine(
                    color = NeonOrange.copy(alpha = 0.4f),
                    start = Offset(0f, size.height * 0.20f),
                    end = Offset(size.width, size.height * 0.20f),
                    strokeWidth = 1.5f
                )

                // Bottom Bucket Zone guide
                drawRect(
                    color = Color.Black.copy(alpha = 0.4f),
                    topLeft = Offset(0f, size.height * 0.86f),
                    size = Size(size.width, size.height * 0.14f)
                )
                drawLine(
                    color = NeonCyan.copy(alpha = 0.4f),
                    start = Offset(0f, size.height * 0.86f),
                    end = Offset(size.width, size.height * 0.86f),
                    strokeWidth = 1.5f
                )

                // Draw Pegs
                pegsList.forEachIndexed { index, peg ->
                    val cx = peg.xRatio * size.width
                    val cy = peg.yRatio * size.height
                    val isSelected = (index == selectedIndex)

                    val color = when (peg.defaultType) {
                        PegType.BLUE -> NeonCyan
                        PegType.ORANGE -> NeonOrange
                        PegType.GREEN -> NeonGreen
                        PegType.PURPLE -> NeonMagenta
                        PegType.MULTIBALL -> Color(0xFF00E5FF)
                        PegType.LONG_AIM -> NeonYellow
                        PegType.BLAST_RADIUS -> Color(0xFFFF3B30)
                    }

                    if (peg.shape == PegShape.CIRCLE) {
                        // Outer neon glow
                        drawCircle(color = color.copy(alpha = 0.35f), radius = peg.width * 1.4f, center = Offset(cx, cy))
                        // Main circle
                        drawCircle(color = color, radius = peg.width, center = Offset(cx, cy))
                        // White highlight
                        drawCircle(color = Color.White.copy(alpha = 0.7f), radius = peg.width * 0.35f, center = Offset(cx - peg.width * 0.25f, cy - peg.width * 0.25f))

                        if (isSelected) {
                            drawCircle(color = NeonYellow, radius = peg.width * 1.8f, center = Offset(cx, cy), style = Stroke(width = 2.5f))
                        }
                    } else {
                        // Brick
                        rotate(degrees = peg.rotation, pivot = Offset(cx, cy)) {
                            val w = peg.width
                            val h = peg.height
                            drawRoundRect(
                                color = color.copy(alpha = 0.35f),
                                topLeft = Offset(cx - w / 2f - 4f, cy - h / 2f - 4f),
                                size = Size(w + 8f, h + 8f),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                            drawRoundRect(
                                color = color,
                                topLeft = Offset(cx - w / 2f, cy - h / 2f),
                                size = Size(w, h),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.7f),
                                topLeft = Offset(cx - w / 2f + 2f, cy - h / 2f + 2f),
                                size = Size(w - 4f, 3f),
                                cornerRadius = CornerRadius(2f, 2f)
                            )

                            if (isSelected) {
                                drawRoundRect(
                                    color = NeonYellow,
                                    topLeft = Offset(cx - w / 2f - 6f, cy - h / 2f - 6f),
                                    size = Size(w + 12f, h + 12f),
                                    cornerRadius = CornerRadius(8f, 8f),
                                    style = Stroke(width = 2.5f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- BOTTOM TOOLS & PALETTE ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1122)),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Row 1: Tool Modes (Add, Select, Erase)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ToolModeButton(
                        label = "Colocar",
                        icon = Icons.Default.Add,
                        active = toolMode == EditorToolMode.ADD,
                        activeColor = NeonGreen
                    ) { toolMode = EditorToolMode.ADD }

                    ToolModeButton(
                        label = "Seleccionar",
                        icon = Icons.Default.Settings,
                        active = toolMode == EditorToolMode.SELECT,
                        activeColor = NeonCyan
                    ) { toolMode = EditorToolMode.SELECT }

                    ToolModeButton(
                        label = "Borrar",
                        icon = Icons.Default.Delete,
                        active = toolMode == EditorToolMode.ERASE,
                        activeColor = Color(0xFFFF5252)
                    ) { toolMode = EditorToolMode.ERASE }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 2: Shape & Type Palettes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Shapes
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShapeChip(
                            label = "Círculo",
                            selected = activeShape == PegShape.CIRCLE
                        ) {
                            activeShape = PegShape.CIRCLE
                            if (selectedIndex in pegsList.indices) {
                                val curr = pegsList[selectedIndex]
                                pegsList[selectedIndex] = curr.copy(shape = PegShape.CIRCLE, width = 15f, height = 15f)
                            }
                        }
                        ShapeChip(
                            label = "Ladrillo",
                            selected = activeShape == PegShape.BRICK
                        ) {
                            activeShape = PegShape.BRICK
                            if (selectedIndex in pegsList.indices) {
                                val curr = pegsList[selectedIndex]
                                pegsList[selectedIndex] = curr.copy(shape = PegShape.BRICK, width = 44f, height = 14f)
                            }
                        }
                    }

                    // Peg Type Color Selector
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PegTypeSwatch(color = NeonCyan, selected = activeType == PegType.BLUE) {
                            activeType = PegType.BLUE
                            if (selectedIndex in pegsList.indices) {
                                val curr = pegsList[selectedIndex]
                                pegsList[selectedIndex] = curr.copy(defaultType = PegType.BLUE)
                            }
                        }
                        PegTypeSwatch(color = NeonOrange, selected = activeType == PegType.ORANGE) {
                            activeType = PegType.ORANGE
                            if (selectedIndex in pegsList.indices) {
                                val curr = pegsList[selectedIndex]
                                pegsList[selectedIndex] = curr.copy(defaultType = PegType.ORANGE)
                            }
                        }
                        PegTypeSwatch(color = NeonGreen, selected = activeType == PegType.GREEN) {
                            activeType = PegType.GREEN
                            if (selectedIndex in pegsList.indices) {
                                val curr = pegsList[selectedIndex]
                                pegsList[selectedIndex] = curr.copy(defaultType = PegType.GREEN)
                            }
                        }
                        PegTypeSwatch(color = NeonMagenta, selected = activeType == PegType.PURPLE) {
                            activeType = PegType.PURPLE
                            if (selectedIndex in pegsList.indices) {
                                val curr = pegsList[selectedIndex]
                                pegsList[selectedIndex] = curr.copy(defaultType = PegType.PURPLE)
                            }
                        }
                        PegTypeSwatch(color = Color(0xFF00E5FF), selected = activeType == PegType.MULTIBALL) {
                            activeType = PegType.MULTIBALL
                            if (selectedIndex in pegsList.indices) {
                                val curr = pegsList[selectedIndex]
                                pegsList[selectedIndex] = curr.copy(defaultType = PegType.MULTIBALL)
                            }
                        }
                        PegTypeSwatch(color = NeonYellow, selected = activeType == PegType.LONG_AIM) {
                            activeType = PegType.LONG_AIM
                            if (selectedIndex in pegsList.indices) {
                                val curr = pegsList[selectedIndex]
                                pegsList[selectedIndex] = curr.copy(defaultType = PegType.LONG_AIM)
                            }
                        }
                        PegTypeSwatch(color = Color(0xFFFF3B30), selected = activeType == PegType.BLAST_RADIUS) {
                            activeType = PegType.BLAST_RADIUS
                            if (selectedIndex in pegsList.indices) {
                                val curr = pegsList[selectedIndex]
                                pegsList[selectedIndex] = curr.copy(defaultType = PegType.BLAST_RADIUS)
                            }
                        }
                    }
                }

                // If Brick is selected, show Rotation Quick Buttons
                if (activeShape == PegShape.BRICK) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Giro:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        listOf(0f, 30f, 45f, 90f, -45f).forEach { rot ->
                            val isCurrent = (activeRotation == rot)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isCurrent) NeonCyan else Color(0xFF1B1E38))
                                    .clickable {
                                        activeRotation = rot
                                        if (selectedIndex in pegsList.indices) {
                                            val curr = pegsList[selectedIndex]
                                            pegsList[selectedIndex] = curr.copy(rotation = rot)
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${rot.toInt()}°",
                                    color = if (isCurrent) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- LEVEL CONFIG DIALOG ---
    if (showConfigDialog) {
        AlertDialog(
            onDismissRequest = { showConfigDialog = false },
            title = {
                Text(
                    text = "Ajustes de Nivel KOF",
                    color = NeonYellow,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = levelTitle,
                        onValueChange = { levelTitle = it },
                        label = { Text("Nombre del Nivel") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0xFF2E335B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = levelSubtitle,
                        onValueChange = { levelSubtitle = it },
                        label = { Text("Subtítulo") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0xFF2E335B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Master Selector
                    Text("Maestro KOF:", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PeggleMaster.values().forEach { master ->
                            val isSelected = (selectedMaster == master)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) NeonGreen.copy(alpha = 0.25f) else Color(0xFF14172E))
                                    .border(1.dp, if (isSelected) NeonGreen else Color.Transparent, RoundedCornerShape(10.dp))
                                    .clickable { selectedMaster = master }
                                    .padding(8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(master.emoji, fontSize = 18.sp)
                                    Text(master.title.take(5), fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }

                    // Background Selector
                    Text("Escenario / Fondo:", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableBackgrounds) { bg ->
                            val isSelected = (selectedBackground.name == bg.name)
                            Box(
                                modifier = Modifier
                                    .size(60.dp, 45.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(2.dp, if (isSelected) NeonOrange else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { selectedBackground = bg }
                            ) {
                                Image(
                                    painter = painterResource(id = bg.resId),
                                    contentDescription = bg.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    // Target Orange Pegs Slider
                    Text("Clavijas Naranjas Objetivo: $targetOrange", color = NeonOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Slider(
                        value = targetOrange.toFloat(),
                        onValueChange = { targetOrange = it.roundToInt() },
                        valueRange = 5f..35f,
                        steps = 29,
                        colors = SliderDefaults.colors(thumbColor = NeonOrange, activeTrackColor = NeonOrange)
                    )

                    // Initial Balls Slider
                    Text("Bolas Iniciales: $initialBalls", color = NeonYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Slider(
                        value = initialBalls.toFloat(),
                        onValueChange = { initialBalls = it.roundToInt() },
                        valueRange = 5f..20f,
                        steps = 14,
                        colors = SliderDefaults.colors(thumbColor = NeonYellow, activeTrackColor = NeonYellow)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showConfigDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Aceptar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF0F1122)
        )
    }

    // --- QUICK PRESETS DIALOG ---
    if (showPresetsDialog) {
        AlertDialog(
            onDismissRequest = { showPresetsDialog = false },
            title = {
                Text(
                    text = "Plantillas Rápidas de Clavijas",
                    color = NeonMagenta,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Elige una formación prediseñada para comenzar:",
                        color = Color(0xFF8B9CB5),
                        fontSize = 13.sp
                    )

                    PresetOptionCard("Arco Iris KOF (3 Arcos)", "32 clavijas curvas con ladrillos laterales") {
                        pegsList.clear()
                        for (r in 0..2) {
                            val radius = 0.22f + r * 0.08f
                            val count = 10 + r * 4
                            for (i in 0..count) {
                                val ang = Math.PI.toFloat() * (0.12f + 0.76f * (i.toFloat() / count))
                                val px = 0.5f + cos(ang) * radius
                                val py = 0.48f - sin(ang) * (radius * 0.9f)
                                pegsList.add(PegTemplate(px, py, defaultType = PegType.BLUE))
                            }
                        }
                        pegsList.add(PegTemplate(0.18f, 0.42f, shape = PegShape.BRICK, width = 42f, height = 13f, rotation = 40f))
                        pegsList.add(PegTemplate(0.82f, 0.42f, shape = PegShape.BRICK, width = 42f, height = 13f, rotation = -40f))
                        showPresetsDialog = false
                    }

                    PresetOptionCard("Diamante Sagrado", "24 clavijas en rombo con estrella central") {
                        pegsList.clear()
                        for (i in 0 until 16) {
                            val ang = (i * Math.PI * 2 / 16).toFloat()
                            val px = 0.5f + cos(ang) * 0.32f
                            val py = 0.45f + sin(ang) * 0.22f
                            pegsList.add(PegTemplate(px, py, defaultType = PegType.BLUE))
                        }
                        for (i in 0 until 8) {
                            val ang = (i * Math.PI * 2 / 8).toFloat()
                            val px = 0.5f + cos(ang) * 0.16f
                            val py = 0.45f + sin(ang) * 0.11f
                            pegsList.add(PegTemplate(px, py, defaultType = PegType.ORANGE))
                        }
                        pegsList.add(PegTemplate(0.5f, 0.45f, defaultType = PegType.GREEN))
                        showPresetsDialog = false
                    }

                    PresetOptionCard("Pirámide de Fuego", "28 clavijas en pirámide escalonada") {
                        pegsList.clear()
                        for (r in 0..6) {
                            val y = 0.28f + r * 0.06f
                            val count = r + 1
                            val startX = 0.5f - r * 0.045f
                            for (c in 0 until count) {
                                val x = startX + c * 0.09f
                                pegsList.add(PegTemplate(x, y, defaultType = if (r % 2 == 1) PegType.ORANGE else PegType.BLUE))
                            }
                        }
                        showPresetsDialog = false
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPresetsDialog = false }) {
                    Text("Cerrar", color = NeonCyan)
                }
            },
            containerColor = Color(0xFF0F1122)
        )
    }
}

// --- HELPER COMPOSABLES ---

@Composable
private fun StatsBadge(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$label: ", color = Color(0xFF8B9CB5), fontSize = 11.sp)
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun ToolModeButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(38.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (active) activeColor else Color(0xFF1B1E38)
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) Color.Black else Color.White,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = if (active) Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ShapeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) NeonCyan else Color(0xFF1B1E38))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun PegTypeSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color)
            .border(if (selected) 2.5.dp else 1.dp, if (selected) Color.White else Color.Black, CircleShape)
            .clickable(onClick = onClick)
    )
}

@Composable
private fun PresetOptionCard(title: String, desc: String, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181B34)),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(desc, color = Color(0xFF8B9CB5), fontSize = 11.sp)
        }
    }
}
