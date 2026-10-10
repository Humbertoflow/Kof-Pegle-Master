package com.example.firebase.model

import com.google.firebase.Timestamp

data class UserProfile(
    val userId: String = "",
    val displayName: String = "",
    val highScore: Int = 0,
    val stagesUnlocked: Int = 1,
    val totalPegsHit: Int = 0,
    val totalFeverCount: Int = 0,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class ScoreRecord(
    val userId: String = "",
    val recordId: String = "",
    val stageId: String = "",
    val stageTitle: String = "",
    val score: Int = 0,
    val masterUsed: String = "",
    val cleared: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
