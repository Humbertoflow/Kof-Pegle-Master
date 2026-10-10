package com.example.peggle.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class PeggleAudioEngine {

    var soundEnabled: Boolean = true
    private val scope = CoroutineScope(Dispatchers.Default)
    private var feverMelodyJob: Job? = null

    // Major Pentatonic scale across 4 octaves for ascending peg hit chimes
    private val pegChimeFrequencies = floatArrayOf(
        261.63f, 293.66f, 329.63f, 392.00f, 440.00f, // C4 to A4
        523.25f, 587.33f, 659.25f, 783.99f, 880.00f, // C5 to A5
        1046.50f, 1174.66f, 1318.51f, 1567.98f, 1760.00f, // C6 to A6
        2093.00f, 2349.32f, 2637.02f, 3135.96f, 3520.00f  // C7 to A7
    )

    // Beethoven's Ode to Joy notes and durations (ms) for Extreme Fever
    private val odeToJoyMelody = listOf(
        Pair(329.63f, 280L), // E4
        Pair(329.63f, 280L), // E4
        Pair(349.23f, 280L), // F4
        Pair(392.00f, 280L), // G4
        Pair(392.00f, 280L), // G4
        Pair(349.23f, 280L), // F4
        Pair(329.63f, 280L), // E4
        Pair(293.66f, 280L), // D4
        Pair(261.63f, 280L), // C4
        Pair(261.63f, 280L), // C4
        Pair(293.66f, 280L), // D4
        Pair(329.63f, 280L), // E4
        Pair(329.63f, 380L), // E4
        Pair(293.66f, 180L), // D4
        Pair(293.66f, 480L), // D4

        Pair(329.63f, 280L), // E4
        Pair(329.63f, 280L), // E4
        Pair(349.23f, 280L), // F4
        Pair(392.00f, 280L), // G4
        Pair(392.00f, 280L), // G4
        Pair(349.23f, 280L), // F4
        Pair(329.63f, 280L), // E4
        Pair(293.66f, 280L), // D4
        Pair(261.63f, 280L), // C4
        Pair(261.63f, 280L), // C4
        Pair(293.66f, 280L), // D4
        Pair(329.63f, 280L), // E4
        Pair(293.66f, 380L), // D4
        Pair(261.63f, 180L), // C4
        Pair(261.63f, 600L)  // C4
    )

    fun playPegHit(hitCountInShot: Int, isOrange: Boolean = false, isPurple: Boolean = false) {
        if (!soundEnabled) return
        scope.launch {
            val freqIndex = (hitCountInShot - 1).coerceIn(0, pegChimeFrequencies.size - 1)
            val baseFreq = pegChimeFrequencies[freqIndex]
            val durationMs = if (isOrange || isPurple) 340 else 220
            val volume = if (isOrange) 0.95f else 0.75f

            playTone(
                frequency = baseFreq,
                durationMs = durationMs,
                volume = volume,
                harmonicRichness = if (isOrange) 0.45f else 0.25f,
                decaySpeed = 7.5f
            )
        }
    }

    fun playCannonFire() {
        if (!soundEnabled) return
        scope.launch {
            // Cannon thud: sweeping frequency from 150 Hz down to 45 Hz with low rumble
            val sampleRate = 22050
            val numSamples = (sampleRate * 0.24).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val freq = 150.0 * (1.0 - t / 0.24) + 40.0
                val sample = sin(2.0 * PI * freq * t) * exp(-t * 9.0)
                buffer[i] = (sample * Short.MAX_VALUE * 0.85).toInt().toShort()
            }
            playRawBuffer(buffer, sampleRate)
        }
    }

    fun playFreeBallBucketCatch() {
        if (!soundEnabled) return
        scope.launch {
            // Triumphant ascending chime arpeggio: G5 -> C6 -> E6
            val arpeggio = listOf(783.99f, 1046.50f, 1318.51f)
            arpeggio.forEach { freq ->
                playTone(freq, durationMs = 280, volume = 0.9f, harmonicRichness = 0.35f, decaySpeed = 6.0f)
                delay(75)
            }
        }
    }

    fun playPowerActivated() {
        if (!soundEnabled) return
        scope.launch {
            // Mystical shimmer chime: chord 587.33f (D5) + 880.0f (A5)
            playTone(587.33f, durationMs = 450, volume = 0.85f, harmonicRichness = 0.5f, decaySpeed = 4.5f)
            playTone(880.00f, durationMs = 450, volume = 0.7f, harmonicRichness = 0.4f, decaySpeed = 4.5f)
        }
    }

    fun playExtremeFeverOdeToJoy() {
        if (!soundEnabled) return
        feverMelodyJob?.cancel()
        feverMelodyJob = scope.launch {
            // Play Beethoven's Ode to Joy fanfare
            for (note in odeToJoyMelody) {
                if (!isActive || !soundEnabled) break
                // Play melody note
                launch {
                    playTone(
                        frequency = note.first,
                        durationMs = note.second.toInt(),
                        volume = 0.9f,
                        harmonicRichness = 0.4f,
                        decaySpeed = 3.5f
                    )
                }
                // Also play soft bass harmony octave below
                launch {
                    playTone(
                        frequency = note.first / 2f,
                        durationMs = note.second.toInt(),
                        volume = 0.45f,
                        harmonicRichness = 0.2f,
                        decaySpeed = 4.0f
                    )
                }
                delay(note.second)
            }
        }
    }

    fun stopFeverMelody() {
        feverMelodyJob?.cancel()
        feverMelodyJob = null
    }

    private fun playTone(
        frequency: Float,
        durationMs: Int,
        volume: Float = 0.8f,
        harmonicRichness: Float = 0.3f,
        decaySpeed: Float = 6.0f
    ) {
        val sampleRate = 22050
        val durationSec = durationMs / 1000.0
        val numSamples = (sampleRate * durationSec).toInt()
        if (numSamples <= 0) return

        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val fundamental = sin(2.0 * PI * frequency * t)
            val harmonic2 = sin(4.0 * PI * frequency * t) * harmonicRichness
            val harmonic3 = sin(6.0 * PI * frequency * t) * (harmonicRichness * 0.5)

            val rawWave = (fundamental + harmonic2 + harmonic3) / (1.0 + harmonicRichness * 1.5)
            val envelope = exp(-t * decaySpeed)
            val sample = rawWave * envelope * volume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        playRawBuffer(buffer, sampleRate)
    }

    private fun playRawBuffer(buffer: ShortArray, sampleRate: Int) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()

            // Release after playback finishes
            val durationMs = (buffer.size.toDouble() / sampleRate * 1000).toLong() + 50L
            scope.launch {
                delay(durationMs)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // AudioTrack allocation fallback
        }
    }
}
