package com.example.peggle.difficulty

import android.content.Context
import com.example.peggle.data.GamePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Manages Dynamic Difficulty Adjustment (DDA).
 * Analyzes the player's recent performance (wins, losses, streaks, remaining balls, stars)
 * across levels to dynamically adapt:
 *   1. Peg arrangement density (adjusts peg density by filtering or adding auxiliary layout pegs).
 *   2. Ball gravity (lighter for players struggling, heavier for high-mastery veterans).
 *   3. Bonus starting balls when assisted.
 */
class DynamicDifficultyManager(
    private val preferences: GamePreferences
) {

    private val _difficultyState = MutableStateFlow(computeInitialState())
    val difficultyState: StateFlow<DynamicDifficultyState> = _difficultyState.asStateFlow()

    private fun computeInitialState(): DynamicDifficultyState {
        val recentHistory = preferences.getRecentPerformanceHistory()
        return calculateDifficultyFromHistory(recentHistory)
    }

    /**
     * Records a level session outcome (either victory or game over).
     */
    fun recordLevelOutcome(
        levelId: String,
        cleared: Boolean,
        ballsRemaining: Int,
        score: Int,
        stars: Int,
        orangeClearedRatio: Float
    ): DynamicDifficultyState {
        val record = LevelPerformanceRecord(
            levelId = levelId,
            cleared = cleared,
            ballsRemaining = ballsRemaining,
            score = score,
            stars = stars,
            orangeClearedRatio = orangeClearedRatio
        )

        preferences.addPerformanceRecord(record)
        val updatedHistory = preferences.getRecentPerformanceHistory()
        val newState = calculateDifficultyFromHistory(updatedHistory)
        _difficultyState.value = newState
        return newState
    }

    /**
     * Computes performance index and resulting rating:
     * - If player has multiple consecutive losses or < 30% win rate -> EASY (assisted: gravity 590, density 0.85, +2 balls)
     * - If player has multiple consecutive wins, high star ratings, or > 75% win rate -> HARD (gravity 760, density 1.15)
     * - Otherwise -> NORMAL (gravity 680, density 1.0)
     */
    fun calculateDifficultyFromHistory(history: List<LevelPerformanceRecord>): DynamicDifficultyState {
        if (history.isEmpty()) {
            return DynamicDifficultyState(
                currentRating = DifficultyRating.NORMAL,
                performanceIndex = 0.5f,
                winStreak = 0,
                lossStreak = 0,
                recentWinRate = 0.5f,
                recentAvgBallsLeft = 2.0f,
                gravity = DifficultyRating.NORMAL.gravity,
                densityMultiplier = DifficultyRating.NORMAL.densityMultiplier
            )
        }

        // Calculate streaks
        var winStreak = 0
        var lossStreak = 0
        var countingWins = history.first().cleared

        for (record in history) {
            if (countingWins) {
                if (record.cleared) winStreak++ else break
            } else {
                if (!record.cleared) lossStreak++ else break
            }
        }

        val totalGames = history.size
        val wins = history.count { it.cleared }
        val winRate = wins.toFloat() / totalGames.toFloat()

        val avgBallsLeft = history.map { it.ballsRemaining }.average().toFloat()
        val avgStars = history.map { it.stars }.average().toFloat()

        // Weighted performance index: 0.0 to 1.0
        // - Win rate (45% weight)
        // - Average balls left normalized to 5 (25% weight)
        // - Average stars normalized to 3 (20% weight)
        // - Streak bias (10% weight)
        val streakBias = when {
            winStreak >= 2 -> 0.15f
            lossStreak >= 2 -> -0.15f
            else -> 0f
        }

        val rawIndex = (winRate * 0.45f) +
                ((avgBallsLeft.coerceIn(0f, 6f) / 6f) * 0.25f) +
                ((avgStars.coerceIn(0f, 3f) / 3f) * 0.20f) +
                streakBias

        val performanceIndex = rawIndex.coerceIn(0.0f, 1.0f)

        val rating = when {
            // Player is struggling (e.g. 2 consecutive losses or low win rate < 0.35)
            lossStreak >= 2 || performanceIndex < 0.36f -> DifficultyRating.EASY
            // Player is dominating (e.g. 3 consecutive wins or high performance index > 0.68)
            (winStreak >= 2 && performanceIndex > 0.65f) || performanceIndex > 0.72f -> DifficultyRating.HARD
            else -> DifficultyRating.NORMAL
        }

        return DynamicDifficultyState(
            currentRating = rating,
            performanceIndex = performanceIndex,
            winStreak = winStreak,
            lossStreak = lossStreak,
            recentWinRate = winRate,
            recentAvgBallsLeft = avgBallsLeft,
            gravity = rating.gravity,
            densityMultiplier = rating.densityMultiplier
        )
    }

    /**
     * Resets performance history back to standard baseline.
     */
    fun resetDifficulty() {
        preferences.clearPerformanceHistory()
        _difficultyState.value = computeInitialState()
    }

    companion object {
        @Volatile
        private var INSTANCE: DynamicDifficultyManager? = null

        fun getInstance(context: Context): DynamicDifficultyManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DynamicDifficultyManager(GamePreferences(context.applicationContext)).also {
                    INSTANCE = it
                }
            }
        }
    }
}
