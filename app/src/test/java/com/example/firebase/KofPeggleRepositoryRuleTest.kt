package com.example.firebase

import com.example.base.FirestoreEmulatorTestBase
import com.example.firebase.model.ScoreRecord
import com.example.firebase.repository.KofPeggleRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class KofPeggleRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun getOrCreateProfile_authenticatedUser_createsAndReturnsProfile() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repo = KofPeggleRepository(firestore)

    val profile = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.getOrCreateProfile("Alice")
    }

    assertEquals(uid, profile.userId)
    assertEquals("Alice", profile.displayName)
    assertEquals(1, profile.stagesUnlocked)
  }

  @Test
  fun saveScoreRecord_authenticatedUser_persistsRecord() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repo = KofPeggleRepository(firestore)
    repo.getOrCreateProfile("Alice")

    val recId = "rec_${System.currentTimeMillis()}"
    val record = ScoreRecord(
      userId = uid,
      recordId = recId,
      stageId = "stage_1",
      stageTitle = "Bosque Sagrado",
      score = 250000,
      masterUsed = "Kyo Kusanagi",
      cleared = true
    )

    val savedId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.saveScoreRecord(record)
    }
    assertEquals(recId, savedId)

    val records = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeScoreRecords().first { list -> list.any { it.recordId == recId } }
    }
    assertTrue(records.any { it.recordId == recId && it.score == 250000 })
  }

  @Test
  fun crossUserAccess_bobCannotReadAliceProfile() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = KofPeggleRepository(firestore)
    aliceRepo.getOrCreateProfile("Alice")

    // Bob signs in
    signInTestUser(BOB_EMAIL)
    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        firestore.collection("users").document(aliceUid).get().await()
      }
      fail("Should fail with PERMISSION_DENIED")
    } catch (e: Exception) {
      val isPermissionDenied = e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
      assertTrue("Exception should be PERMISSION_DENIED: $e", isPermissionDenied)
    }
  }

  @Test
  fun unauthenticatedUser_failsWithException() = runBlocking {
    auth.signOut()
    val repo = KofPeggleRepository(firestore)

    try {
      repo.requireUserId()
      fail("Should throw IllegalStateException")
    } catch (e: IllegalStateException) {
      assertNotNull(e.message)
    }
  }

  companion object {
    private const val ALICE_EMAIL = "alice_kof@test.com"
    private const val BOB_EMAIL = "bob_kof@test.com"
    private const val DEFAULT_TIMEOUT_MS = 6000L
    private const val FLOW_TIMEOUT_MS = 5000L
  }
}
