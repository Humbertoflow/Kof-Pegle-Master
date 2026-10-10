package com.example.peggle.data

import com.example.peggle.model.Peg
import com.example.peggle.model.PegShape
import com.example.peggle.model.PegType
import com.example.peggle.model.PeggleMaster
import kotlin.math.cos
import kotlin.math.sin

data class PegTemplate(
    val xRatio: Float, // 0.0f to 1.0f relative to board width
    val yRatio: Float, // 0.0f to 1.0f relative to board height
    val defaultType: PegType = PegType.BLUE,
    val shape: PegShape = PegShape.CIRCLE,
    val width: Float = 14f,
    val height: Float = 14f,
    val rotation: Float = 0f
)

data class LevelData(
    val id: String,
    val stageNumber: Int,
    val title: String,
    val subtitle: String,
    val master: PeggleMaster,
    val backgroundGradient: Pair<Long, Long>,
    val bgDrawableRes: Int,
    val templates: List<PegTemplate>
)

object LevelCatalog {

    fun getAllLevels(): List<LevelData> = listOf(
        createLevel1(),
        createLevel2(),
        createLevel3(),
        createLevel4(),
        createLevel5(),
        createLevel6(),
        createLevel7(),
        createLevel8()
    ) + CampaignLevels.getAdditionalLevels()

    fun getLevelById(id: String): LevelData {
        return getAllLevels().find { it.id == id } ?: getAllLevels().first()
    }

    /**
     * Converts level templates into actual runtime pegs scaled to board dimensions.
     * Ensures exactly 25 Orange pegs and 2 Green pegs exist!
     */
    fun instantiatePegs(level: LevelData, boardWidth: Float, boardHeight: Float): List<Peg> {
        val pegs = mutableListOf<Peg>()
        val templates = level.templates

        // Collect indices for assignment
        val allIndices = templates.indices.shuffled()
        val orangeIndices = mutableSetOf<Int>()
        val greenIndices = mutableSetOf<Int>()

        // Pick 25 orange pegs
        val targetOrange = 25.coerceAtMost(templates.size - 5)
        for (i in 0 until targetOrange) {
            orangeIndices.add(allIndices[i])
        }

        // Pick 2 green pegs
        for (i in targetOrange until targetOrange + 2) {
            if (i < allIndices.size) {
                greenIndices.add(allIndices[i])
            }
        }

        // Pick special power-up pegs
        val multiballIndices = mutableSetOf<Int>()
        val longAimIndices = mutableSetOf<Int>()
        val blastRadiusIndices = mutableSetOf<Int>()

        var nextIndex = targetOrange + 2
        if (nextIndex < allIndices.size) multiballIndices.add(allIndices[nextIndex++])
        if (nextIndex < allIndices.size) longAimIndices.add(allIndices[nextIndex++])
        if (nextIndex < allIndices.size) blastRadiusIndices.add(allIndices[nextIndex++])

        var pegId = 0
        templates.forEachIndexed { index, t ->
            val type = when {
                orangeIndices.contains(index) -> PegType.ORANGE
                greenIndices.contains(index) -> PegType.GREEN
                multiballIndices.contains(index) -> PegType.MULTIBALL
                longAimIndices.contains(index) -> PegType.LONG_AIM
                blastRadiusIndices.contains(index) -> PegType.BLAST_RADIUS
                t.defaultType.isSpecialPowerUp -> t.defaultType
                else -> PegType.BLUE
            }

            pegs.add(
                Peg(
                    id = pegId++,
                    x = t.xRatio * boardWidth,
                    y = t.yRatio * boardHeight,
                    type = type,
                    shape = t.shape,
                    width = t.width,
                    height = t.height,
                    rotation = t.rotation
                )
            )
        }

        return pegs
    }

    // Level 1: Bosque de Peggle - Arco Iris (Bjorn)
    private fun createLevel1(): LevelData {
        val list = mutableListOf<PegTemplate>()

        // Rainbow Arches
        val archRadii = listOf(0.24f, 0.32f, 0.40f)
        val centerX = 0.5f
        val centerY = 0.48f

        archRadii.forEachIndexed { rIdx, radius ->
            val count = 10 + rIdx * 4
            for (i in 0..count) {
                val angle = Math.PI.toFloat() * (0.12f + 0.76f * (i.toFloat() / count))
                val px = centerX + cos(angle) * radius
                val py = centerY - sin(angle) * (radius * 0.9f)
                list.add(PegTemplate(px, py, width = 14f, height = 14f))
            }
        }

        // Bottom pinball bumpers and flower clusters
        for (col in 0..6) {
            val x = 0.2f + col * 0.1f
            val y = 0.58f + (col % 2) * 0.05f
            list.add(PegTemplate(x, y, shape = PegShape.CIRCLE, width = 15f, height = 15f))
        }

        for (col in 0..5) {
            val x = 0.25f + col * 0.1f
            val y = 0.68f + ((col + 1) % 2) * 0.04f
            list.add(PegTemplate(x, y, shape = PegShape.CIRCLE, width = 14f, height = 14f))
        }

        // Bricks on lateral shields
        list.add(PegTemplate(0.12f, 0.42f, shape = PegShape.BRICK, width = 38f, height = 14f, rotation = 45f))
        list.add(PegTemplate(0.88f, 0.42f, shape = PegShape.BRICK, width = 38f, height = 14f, rotation = -45f))
        list.add(PegTemplate(0.16f, 0.56f, shape = PegShape.BRICK, width = 38f, height = 14f, rotation = -30f))
        list.add(PegTemplate(0.84f, 0.56f, shape = PegShape.BRICK, width = 38f, height = 14f, rotation = 30f))

        // Center cluster
        list.add(PegTemplate(0.5f, 0.38f, shape = PegShape.CIRCLE, width = 16f, height = 16f))
        list.add(PegTemplate(0.44f, 0.42f, shape = PegShape.CIRCLE, width = 14f, height = 14f))
        list.add(PegTemplate(0.56f, 0.42f, shape = PegShape.CIRCLE, width = 14f, height = 14f))

        return LevelData(
            id = "level_1",
            stageNumber = 1,
            title = "KOF Arena - Llamas Kusanagi",
            subtitle = "Bjorn domina las llamas solares de Kyo",
            master = PeggleMaster.BJORN,
            backgroundGradient = Pair(0xFF132B47, 0xFF19402A),
            bgDrawableRes = com.example.R.drawable.bg_kof_kyo,
            templates = list
        )
    }

    // Level 2: Flor de Loto - Cascadas (Bjorn)
    private fun createLevel2(): LevelData {
        val list = mutableListOf<PegTemplate>()

        // 3 Petal flowers
        val centers = listOf(Pair(0.28f, 0.34f), Pair(0.72f, 0.34f), Pair(0.5f, 0.54f))
        centers.forEach { (cx, cy) ->
            list.add(PegTemplate(cx, cy, width = 16f, height = 16f))
            for (p in 0 until 8) {
                val ang = (p * Math.PI * 2 / 8).toFloat()
                val px = cx + cos(ang) * 0.11f
                val py = cy + sin(ang) * 0.08f
                list.add(PegTemplate(px, py, width = 13f, height = 13f))
            }
        }

        // Brick stems and ramps
        list.add(PegTemplate(0.28f, 0.46f, shape = PegShape.BRICK, width = 42f, height = 12f, rotation = 90f))
        list.add(PegTemplate(0.72f, 0.46f, shape = PegShape.BRICK, width = 42f, height = 12f, rotation = 90f))
        list.add(PegTemplate(0.5f, 0.66f, shape = PegShape.BRICK, width = 46f, height = 12f, rotation = 0f))

        // Dropping fields
        for (r in 0..3) {
            val y = 0.70f + r * 0.05f
            val count = 8 - (r % 2)
            val startX = if (r % 2 == 0) 0.18f else 0.22f
            for (c in 0 until count) {
                list.add(PegTemplate(startX + c * 0.09f, y, width = 13f, height = 13f))
            }
        }

        return LevelData(
            id = "level_2",
            stageNumber = 2,
            title = "South Town - Pelea Callejera",
            subtitle = "Rebotes de alta energía estilo Terry Bogard",
            master = PeggleMaster.BJORN,
            backgroundGradient = Pair(0xFF1B2A4A, 0xFF2B4D3F),
            bgDrawableRes = com.example.R.drawable.bg_kof_southtown,
            templates = list
        )
    }

    // Level 3: Mansión Espeluznante - Jack-O'-Lantern (Renfield)
    private fun createLevel3(): LevelData {
        val list = mutableListOf<PegTemplate>()

        // Pumpkin outline
        val cx = 0.5f
        val cy = 0.44f
        for (i in 0 until 18) {
            val ang = (i * Math.PI * 2 / 18).toFloat()
            val rx = 0.36f
            val ry = 0.24f
            val px = cx + cos(ang) * rx
            val py = cy + sin(ang) * ry
            list.add(PegTemplate(px, py, width = 14f, height = 14f))
        }

        // Triangle eyes (bricks)
        list.add(PegTemplate(0.38f, 0.38f, shape = PegShape.BRICK, width = 28f, height = 12f, rotation = -20f))
        list.add(PegTemplate(0.62f, 0.38f, shape = PegShape.BRICK, width = 28f, height = 12f, rotation = 20f))

        // Nose peg
        list.add(PegTemplate(0.5f, 0.43f, shape = PegShape.CIRCLE, width = 15f, height = 15f))

        // Jagged smile pegs
        val smileXs = listOf(0.34f, 0.40f, 0.46f, 0.50f, 0.54f, 0.60f, 0.66f)
        val smileYs = listOf(0.52f, 0.54f, 0.52f, 0.55f, 0.52f, 0.54f, 0.52f)
        smileXs.forEachIndexed { i, sx ->
            list.add(PegTemplate(sx, smileYs[i], width = 14f, height = 14f))
        }

        // Crypt columns
        for (i in 0..5) {
            list.add(PegTemplate(0.14f, 0.62f + i * 0.04f, width = 13f, height = 13f))
            list.add(PegTemplate(0.86f, 0.62f + i * 0.04f, width = 13f, height = 13f))
        }

        // Lower hopper
        for (c in 0..6) {
            list.add(PegTemplate(0.24f + c * 0.09f, 0.76f + (c % 2) * 0.03f, width = 14f, height = 14f))
        }

        return LevelData(
            id = "level_3",
            stageNumber = 3,
            title = "Santuario Shiranui - Danza Ninja",
            subtitle = "Renfield esquiva los abanicos de fuego de Mai",
            master = PeggleMaster.RENFIELD,
            backgroundGradient = Pair(0xFF2B1238, 0xFF4A1E14),
            bgDrawableRes = com.example.R.drawable.bg_kof_mai,
            templates = list
        )
    }

    // Level 4: Pasaje de las Sombras (Renfield)
    private fun createLevel4(): LevelData {
        val list = mutableListOf<PegTemplate>()

        // Diagonal brick slats creating a zigzag pinball slide
        val slats = listOf(
            Triple(0.25f, 0.28f, 30f),
            Triple(0.75f, 0.36f, -30f),
            Triple(0.30f, 0.46f, 30f),
            Triple(0.70f, 0.56f, -30f)
        )
        slats.forEach { (sx, sy, rot) ->
            list.add(PegTemplate(sx, sy, shape = PegShape.BRICK, width = 58f, height = 14f, rotation = rot))
            // Peg clusters surrounding slats
            list.add(PegTemplate(sx - 0.08f, sy - 0.04f, width = 13f, height = 13f))
            list.add(PegTemplate(sx + 0.08f, sy - 0.04f, width = 13f, height = 13f))
            list.add(PegTemplate(sx, sy + 0.05f, width = 14f, height = 14f))
        }

        // Field of hanging orbs
        for (r in 0..3) {
            val y = 0.64f + r * 0.05f
            val count = 7
            for (c in 0 until count) {
                val x = 0.22f + c * 0.10f + (if (r % 2 == 1) 0.05f else 0f)
                if (x <= 0.88f) {
                    list.add(PegTemplate(x, y, width = 14f, height = 14f))
                }
            }
        }

        return LevelData(
            id = "level_4",
            stageNumber = 4,
            title = "Luna Carmesí - Llamas de Iori",
            subtitle = "El poder maldito del fuego púrpura Yasakani",
            master = PeggleMaster.RENFIELD,
            backgroundGradient = Pair(0xFF1E1128, 0xFF3D1630),
            bgDrawableRes = com.example.R.drawable.bg_kof_iori,
            templates = list
        )
    }

    // Level 5: Pirámide de Oro (Kat Tut)
    private fun createLevel5(): LevelData {
        val list = mutableListOf<PegTemplate>()

        // Triangular pyramid of pegs
        val rows = 8
        val topY = 0.28f
        for (r in 0 until rows) {
            val y = topY + r * 0.052f
            val count = r + 1
            val startX = 0.5f - (r * 0.05f)
            for (c in 0 until count) {
                val x = startX + c * 0.10f
                list.add(PegTemplate(x, y, width = 14f, height = 14f))
            }
        }

        // Pyramid protective golden bricks on the sides
        for (b in 0..3) {
            val by = 0.35f + b * 0.08f
            list.add(PegTemplate(0.18f, by, shape = PegShape.BRICK, width = 36f, height = 13f, rotation = 55f))
            list.add(PegTemplate(0.82f, by, shape = PegShape.BRICK, width = 36f, height = 13f, rotation = -55f))
        }

        // Base golden gates
        for (c in 0..7) {
            list.add(PegTemplate(0.18f + c * 0.09f, 0.74f, width = 14f, height = 14f))
        }

        return LevelData(
            id = "level_5",
            stageNumber = 5,
            title = "Dojo Kusanagi - Furia Solar",
            subtitle = "Kat Tut invoca el escudo dorado KOF",
            master = PeggleMaster.KAT_TUT,
            backgroundGradient = Pair(0xFF33200B, 0xFF543D16),
            bgDrawableRes = com.example.R.drawable.bg_kof_kyo,
            templates = list
        )
    }

    // Level 6: Ojo de Horus (Kat Tut)
    private fun createLevel6(): LevelData {
        val list = mutableListOf<PegTemplate>()

        // Concentric diamond
        val diamondPoints = listOf(
            Pair(0.5f, 0.26f),
            Pair(0.72f, 0.44f),
            Pair(0.5f, 0.62f),
            Pair(0.28f, 0.44f)
        )

        // Diamond perimeter
        for (i in 0 until 16) {
            val frac = i / 16f
            val seg = (frac * 4).toInt()
            val subFrac = (frac * 4) - seg
            val p1 = diamondPoints[seg % 4]
            val p2 = diamondPoints[(seg + 1) % 4]
            val px = p1.first + (p2.first - p1.first) * subFrac
            val py = p1.second + (p2.second - p1.second) * subFrac
            list.add(PegTemplate(px, py, width = 14f, height = 14f))
        }

        // Inner Eye iris
        for (i in 0 until 8) {
            val ang = (i * Math.PI * 2 / 8).toFloat()
            val px = 0.5f + cos(ang) * 0.09f
            val py = 0.44f + sin(ang) * 0.07f
            list.add(PegTemplate(px, py, width = 15f, height = 15f))
        }
        list.add(PegTemplate(0.5f, 0.44f, width = 17f, height = 17f))

        // Side bricks
        list.add(PegTemplate(0.16f, 0.44f, shape = PegShape.BRICK, width = 45f, height = 14f, rotation = 90f))
        list.add(PegTemplate(0.84f, 0.44f, shape = PegShape.BRICK, width = 45f, height = 14f, rotation = 90f))

        // Lower chutes
        for (c in 0..7) {
            list.add(PegTemplate(0.18f + c * 0.09f, 0.72f + (c % 2) * 0.03f, width = 13f, height = 13f))
        }

        return LevelData(
            id = "level_6",
            stageNumber = 6,
            title = "Templo Shiranui - Cerezos en Flor",
            subtitle = "Kat Tut y la precisión ninja de Mai Shiranui",
            master = PeggleMaster.KAT_TUT,
            backgroundGradient = Pair(0xFF382510, 0xFF634718),
            bgDrawableRes = com.example.R.drawable.bg_kof_mai,
            templates = list
        )
    }

    // Level 7: Volcán Ardiente (Master Sploder)
    private fun createLevel7(): LevelData {
        val list = mutableListOf<PegTemplate>()

        // Volcanic caldera with dense explosive cluster
        for (r in 0..6) {
            val y = 0.28f + r * 0.055f
            val count = 8 - (r % 2)
            val startX = if (r % 2 == 0) 0.18f else 0.22f
            for (c in 0 until count) {
                list.add(PegTemplate(startX + c * 0.09f, y, width = 14f, height = 14f))
            }
        }

        // Lava barrier bricks
        list.add(PegTemplate(0.30f, 0.68f, shape = PegShape.BRICK, width = 46f, height = 14f, rotation = -25f))
        list.add(PegTemplate(0.70f, 0.68f, shape = PegShape.BRICK, width = 46f, height = 14f, rotation = 25f))

        // Bottom pinball bumpers
        for (c in 0..5) {
            list.add(PegTemplate(0.25f + c * 0.10f, 0.77f, width = 15f, height = 15f))
        }

        return LevelData(
            id = "level_7",
            stageNumber = 7,
            title = "Furia de Iori - Instinto Salvaje",
            subtitle = "¡Master Sploder hace estallar el fuego Yasakani!",
            master = PeggleMaster.MASTER_SPLODER,
            backgroundGradient = Pair(0xFF45140E, 0xFF692418),
            bgDrawableRes = com.example.R.drawable.bg_kof_iori,
            templates = list
        )
    }

    // Level 8: La Ruleta del Gran Maestro (Warren)
    private fun createLevel8(): LevelData {
        val list = mutableListOf<PegTemplate>()

        // Double concentric wheel
        val cx = 0.5f
        val cy = 0.44f

        for (i in 0 until 14) {
            val ang = (i * Math.PI * 2 / 14).toFloat()
            val px = cx + cos(ang) * 0.32f
            val py = cy + sin(ang) * 0.22f
            list.add(PegTemplate(px, py, width = 14f, height = 14f))
        }

        for (i in 0 until 10) {
            val ang = (i * Math.PI * 2 / 10).toFloat()
            val px = cx + cos(ang) * 0.19f
            val py = cy + sin(ang) * 0.13f
            list.add(PegTemplate(px, py, width = 14f, height = 14f))
        }

        // Center star
        list.add(PegTemplate(cx, cy, width = 18f, height = 18f))

        // Magic Hat bricks
        list.add(PegTemplate(0.22f, 0.68f, shape = PegShape.BRICK, width = 42f, height = 14f, rotation = 35f))
        list.add(PegTemplate(0.78f, 0.68f, shape = PegShape.BRICK, width = 42f, height = 14f, rotation = -35f))

        // Bottom spread
        for (c in 0..6) {
            list.add(PegTemplate(0.23f + c * 0.09f, 0.76f + (c % 2) * 0.03f, width = 14f, height = 14f))
        }

        return LevelData(
            id = "level_8",
            stageNumber = 8,
            title = "Torneo Supremo - Dimensión Orochi",
            subtitle = "Warren gira la ruleta del gran torneo KOF",
            master = PeggleMaster.WARREN,
            backgroundGradient = Pair(0xFF28133E, 0xFF431F6B),
            bgDrawableRes = com.example.R.drawable.bg_kof_orochi,
            templates = list
        )
    }
}
