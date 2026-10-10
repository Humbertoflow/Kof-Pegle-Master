package com.example.peggle.data

import android.content.Context
import android.content.SharedPreferences

class GamePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("peggle_deluxe_prefs", Context.MODE_PRIVATE)

    fun getLevelHighScore(levelId: String): Int {
        return prefs.getInt("high_score_$levelId", 0)
    }

    fun setLevelHighScore(levelId: String, score: Int) {
        val current = getLevelHighScore(levelId)
        if (score > current) {
            prefs.edit().putInt("high_score_$levelId", score).apply()
        }
    }

    fun getLevelStars(levelId: String): Int {
        return prefs.getInt("stars_$levelId", 0)
    }

    fun setLevelStars(levelId: String, stars: Int) {
        val current = getLevelStars(levelId)
        if (stars > current) {
            prefs.edit().putInt("stars_$levelId", stars).apply()
        }
    }

    fun isLevelCleared(levelId: String): Boolean {
        return prefs.getBoolean("cleared_$levelId", false)
    }

    fun setLevelCleared(levelId: String) {
        prefs.edit().putBoolean("cleared_$levelId", true).apply()
    }

    fun isMasterClear(levelId: String): Boolean {
        return prefs.getBoolean("master_clear_$levelId", false)
    }

    fun setMasterClear(levelId: String) {
        prefs.edit().putBoolean("master_clear_$levelId", true).apply()
    }

    fun isSoundEnabled(): Boolean = prefs.getBoolean("sound_enabled", true)
    fun setSoundEnabled(enabled: Boolean) = prefs.edit().putBoolean("sound_enabled", enabled).apply()

    fun isHapticsEnabled(): Boolean = prefs.getBoolean("haptics_enabled", true)
    fun setHapticsEnabled(enabled: Boolean) = prefs.edit().putBoolean("haptics_enabled", enabled).apply()

    // Dynamic Difficulty Performance Tracking
    fun getRecentPerformanceHistory(maxRecords: Int = 10): List<com.example.peggle.difficulty.LevelPerformanceRecord> {
        val serialized = prefs.getString("performance_history_csv", null) ?: return emptyList()
        val records = mutableListOf<com.example.peggle.difficulty.LevelPerformanceRecord>()
        try {
            val lines = serialized.split(";")
            for (line in lines) {
                if (line.isBlank()) continue
                val parts = line.split(",")
                if (parts.size >= 6) {
                    val levelId = parts[0]
                    val cleared = parts[1].toBoolean()
                    val ballsRemaining = parts[2].toIntOrNull() ?: 0
                    val score = parts[3].toIntOrNull() ?: 0
                    val stars = parts[4].toIntOrNull() ?: 0
                    val orangeClearedRatio = parts[5].toFloatOrNull() ?: 0f
                    val timestamp = if (parts.size >= 7) parts[6].toLongOrNull() ?: 0L else 0L
                    records.add(
                        com.example.peggle.difficulty.LevelPerformanceRecord(
                            levelId = levelId,
                            cleared = cleared,
                            ballsRemaining = ballsRemaining,
                            score = score,
                            stars = stars,
                            orangeClearedRatio = orangeClearedRatio,
                            timestamp = timestamp
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Safe fallback
        }
        return records.take(maxRecords)
    }

    fun addPerformanceRecord(record: com.example.peggle.difficulty.LevelPerformanceRecord, maxRecords: Int = 10) {
        val current = getRecentPerformanceHistory(maxRecords).toMutableList()
        current.add(0, record) // Most recent first
        val trimmed = current.take(maxRecords)
        val serialized = trimmed.joinToString(";") {
            "${it.levelId},${it.cleared},${it.ballsRemaining},${it.score},${it.stars},${it.orangeClearedRatio},${it.timestamp}"
        }
        prefs.edit().putString("performance_history_csv", serialized).apply()
    }

    fun clearPerformanceHistory() {
        prefs.edit().remove("performance_history_csv").apply()
    }
}
