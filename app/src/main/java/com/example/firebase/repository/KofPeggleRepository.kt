package com.example.firebase.repository

import android.content.Context
import com.example.R
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.example.firebase.model.ScoreRecord
import com.example.firebase.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class KofPeggleRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth get() = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun getOrCreateProfile(displayName: String): UserProfile {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val snapshot = docRef.get().await()

        return if (snapshot.exists()) {
            snapshot.toObject(UserProfile::class.java) ?: UserProfile(userId = uid, displayName = displayName)
        } else {
            val initialProfile = UserProfile(
                userId = uid,
                displayName = displayName.ifBlank { "Luchador KOF" },
                highScore = 0,
                stagesUnlocked = 1,
                totalPegsHit = 0,
                totalFeverCount = 0
            )
            val payload = mapOf(
                "userId" to initialProfile.userId,
                "displayName" to initialProfile.displayName,
                "highScore" to initialProfile.highScore,
                "stagesUnlocked" to initialProfile.stagesUnlocked,
                "totalPegsHit" to initialProfile.totalPegsHit,
                "totalFeverCount" to initialProfile.totalFeverCount,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                docRef.set(payload).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, docRef.path)
                throw e
            }
            initialProfile
        }
    }

    fun observeProfile(): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) snapshot.toObject(UserProfile::class.java) else null
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    suspend fun updateProgress(
        score: Int,
        stageNumber: Int,
        pegsHit: Int,
        feverCount: Int
    ) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val path = docRef.path

        try {
            val current = docRef.get().await().toObject(UserProfile::class.java)
            val currentHigh = current?.highScore ?: 0
            val currentUnlocked = current?.stagesUnlocked ?: 1
            val currentPegs = current?.totalPegsHit ?: 0
            val currentFevers = current?.totalFeverCount ?: 0

            val updates = mutableMapOf<String, Any>(
                "highScore" to maxOf(currentHigh, score),
                "stagesUnlocked" to maxOf(currentUnlocked, stageNumber),
                "totalPegsHit" to (currentPegs + pegsHit),
                "totalFeverCount" to (currentFevers + feverCount),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updates).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }

    suspend fun saveScoreRecord(record: ScoreRecord): String {
        val uid = requireUserId()
        val recordId = record.recordId.ifBlank { "rec_${System.currentTimeMillis()}" }
        val docRef = db.collection("users").document(uid).collection("records").document(recordId)
        val path = docRef.path

        val payload = mapOf(
            "userId" to uid,
            "recordId" to recordId,
            "stageId" to record.stageId,
            "stageTitle" to record.stageTitle,
            "score" to record.score,
            "masterUsed" to record.masterUsed,
            "cleared" to record.cleared,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
            return recordId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    fun observeScoreRecords(): Flow<List<ScoreRecord>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/records"
        emitAll(
            db.collection("users").document(uid).collection("records")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(ScoreRecord::class.java)
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }
}
