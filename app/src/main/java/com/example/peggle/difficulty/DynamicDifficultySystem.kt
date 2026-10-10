package com.example.peggle.difficulty

/**
 * Difficulty level determined dynamically based on player performance.
 */
enum class DifficultyRating(
    val title: String,
    val subtitle: String,
    val tagText: String,
    val colorHex: Long,
    val densityMultiplier: Float, // Multiplier for optional/extra layout pegs (e.g. 0.85f to 1.15f)
    val gravity: Float,           // Ball gravity in px/s^2 (Standard is 680f)
    val extraFreeBalls: Int       // Extra starting balls if struggling
) {
    EASY(
        title = "Asistido",
        subtitle = "Menor densidad de obstáculos y gravedad reducida",
        tagText = "MODO ASISTIDO",
        colorHex = 0xFF00FF66,
        densityMultiplier = 0.85f,
        gravity = 590f,
        extraFreeBalls = 2
    ),
    NORMAL(
        title = "Equilibrado",
        subtitle = "Físicas estándar de Peggle Deluxe",
        tagText = "EQUILIBRADO",
        colorHex = 0xFF00F0FF,
        densityMultiplier = 1.0f,
        gravity = 680f,
        extraFreeBalls = 0
    ),
    HARD(
        title = "Desafío Maestro",
        subtitle = "Mayor densidad de clavijas y gravedad pesada",
        tagText = "DESAFÍO PRO",
        colorHex = 0xFFFF007F,
        densityMultiplier = 1.15f,
        gravity = 760f,
        extraFreeBalls = 0
    )
}

/**
 * Result record for a single level attempt used to compute rolling performance.
 */
data class LevelPerformanceRecord(
    val levelId: String,
    val cleared: Boolean,
    val ballsRemaining: Int,
    val score: Int,
    val stars: Int,
    val orangeClearedRatio: Float,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * State representing current dynamic difficulty metrics.
 */
data class DynamicDifficultyState(
    val currentRating: DifficultyRating = DifficultyRating.NORMAL,
    val performanceIndex: Float = 0.5f, // 0.0 (struggling) to 1.0 (mastery)
    val winStreak: Int = 0,
    val lossStreak: Int = 0,
    val recentWinRate: Float = 0.5f,
    val recentAvgBallsLeft: Float = 2.0f,
    val gravity: Float = 680f,
    val densityMultiplier: Float = 1.0f
)
