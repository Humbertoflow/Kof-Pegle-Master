package com.example.peggle

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.peggle.audio.SoundManager
import com.example.peggle.model.PegType
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SoundManagerTest {

    private lateinit var soundManager: SoundManager

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        soundManager = SoundManager(context)
    }

    @After
    fun tearDown() {
        soundManager.release()
    }

    @Test
    fun soundManager_initializesAndHandlesToggle() {
        assertNotNull(soundManager)
        assertTrue(soundManager.soundEnabled)

        soundManager.soundEnabled = false
        assertFalse(soundManager.soundEnabled)

        soundManager.soundEnabled = true
        assertTrue(soundManager.soundEnabled)
    }

    @Test
    fun playPegHit_executesWithoutExceptionsForAllPegTypes() {
        // Blue peg with ascending pitch
        soundManager.playPegHit(hitCountInShot = 1, pegType = PegType.BLUE)
        soundManager.playPegHit(hitCountInShot = 5, pegType = PegType.BLUE)
        soundManager.playPegHit(hitCountInShot = 15, pegType = PegType.BLUE)

        // Orange, Green, Purple
        soundManager.playPegHit(hitCountInShot = 2, pegType = PegType.ORANGE)
        soundManager.playPegHit(hitCountInShot = 3, pegType = PegType.GREEN)
        soundManager.playPegHit(hitCountInShot = 4, pegType = PegType.PURPLE)
    }

    @Test
    fun playBucketCatch_andCannon_executesSuccessfully() {
        soundManager.playBucketCatch()
        soundManager.playCannonFire()
        soundManager.playPegPop()
    }

    @Test
    fun kofSoundEffects_executeSuccessfully() {
        soundManager.playKofFlameBurst()
        soundManager.playKofHeavyImpact()
        soundManager.playKofSuperFlash()
        soundManager.playKofReadyGo()
        soundManager.playKofKoVictory()
    }
}
