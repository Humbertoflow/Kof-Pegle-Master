package com.example.peggle.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.R
import com.example.peggle.model.PegType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.random.Random

/**
 * SoundManager handles all sound effects in Kof Peggle using direct PCM AudioTrack
 * playback for low-latency, zero-codec audio.
 *
 * By bypassing MediaCodec/SoundPool and decoding PCM directly into static AudioTrack
 * buffers, it avoids hardware codec queries ("Failed to query component interface")
 * while delivering authentic The King of Fighters style arcade cues and musical chimes.
 */
class SoundManager(context: Context) {

    var soundEnabled: Boolean = true
    private val scope = CoroutineScope(Dispatchers.Default)
    private var feverMelodyJob: Job? = null

    private data class PcmAudio(
        val samples: ShortArray,
        val sampleRate: Int = 44100
    )

    // Loaded Sound Buffers
    private var pcmPegHit: PcmAudio? = null
    private var pcmOrangePeg: PcmAudio? = null
    private var pcmGreenPeg: PcmAudio? = null
    private var pcmPurplePeg: PcmAudio? = null
    private var pcmBucketCatch: PcmAudio? = null
    private var pcmCannonShot: PcmAudio? = null
    private var pcmPegPop: PcmAudio? = null

    // The King of Fighters (KOF) Arcade Style Audio Cues
    private var pcmKofFlame: PcmAudio? = null
    private var pcmKofHeavyImpact: PcmAudio? = null
    private var pcmKofSuperFlash: PcmAudio? = null
    private var pcmKofReadyGo: PcmAudio? = null
    private var pcmKofKo: PcmAudio? = null

    // Ode to Joy melody pairs (Frequency in Hz, Duration in ms)
    private val odeToJoyMelody = listOf(
        Pair(329.63f, 280L), Pair(329.63f, 280L), Pair(349.23f, 280L), Pair(392.00f, 280L),
        Pair(392.00f, 280L), Pair(349.23f, 280L), Pair(329.63f, 280L), Pair(293.66f, 280L),
        Pair(261.63f, 280L), Pair(261.63f, 280L), Pair(293.66f, 280L), Pair(329.63f, 280L),
        Pair(329.63f, 380L), Pair(293.66f, 180L), Pair(293.66f, 480L),
        Pair(329.63f, 280L), Pair(329.63f, 280L), Pair(349.23f, 280L), Pair(392.00f, 280L),
        Pair(392.00f, 280L), Pair(349.23f, 280L), Pair(329.63f, 280L), Pair(293.66f, 280L),
        Pair(261.63f, 280L), Pair(261.63f, 280L), Pair(293.66f, 280L), Pair(329.63f, 280L),
        Pair(293.66f, 380L), Pair(261.63f, 180L), Pair(261.63f, 600L)
    )

    init {
        // Preload PCM assets in background thread without triggering hardware codecs
        scope.launch {
            try {
                pcmPegHit = loadWavPcm(context, R.raw.sfx_peg_hit)
                pcmOrangePeg = loadWavPcm(context, R.raw.sfx_orange_peg)
                pcmGreenPeg = loadWavPcm(context, R.raw.sfx_green_peg)
                pcmPurplePeg = loadWavPcm(context, R.raw.sfx_purple_peg)
                pcmBucketCatch = loadWavPcm(context, R.raw.sfx_bucket_catch)
                pcmCannonShot = loadWavPcm(context, R.raw.sfx_cannon_shot)
                pcmPegPop = loadWavPcm(context, R.raw.sfx_peg_pop)

                pcmKofFlame = loadWavPcm(context, R.raw.sfx_kof_flame_burst)
                pcmKofHeavyImpact = loadWavPcm(context, R.raw.sfx_kof_heavy_impact)
                pcmKofSuperFlash = loadWavPcm(context, R.raw.sfx_kof_super_flash)
                pcmKofReadyGo = loadWavPcm(context, R.raw.sfx_kof_ready_go)
                pcmKofKo = loadWavPcm(context, R.raw.sfx_kof_ko)
            } catch (_: Exception) {}
        }
    }

    private fun loadWavPcm(context: Context, resId: Int): PcmAudio? {
        return try {
            context.resources.openRawResource(resId).use { stream ->
                val bytes = stream.readBytes()
                if (bytes.size < 44) return null
                if (bytes[0] != 'R'.code.toByte() || bytes[1] != 'I'.code.toByte() ||
                    bytes[2] != 'F'.code.toByte() || bytes[3] != 'F'.code.toByte()
                ) {
                    return null
                }
                val sampleRate = (bytes[24].toInt() and 0xFF) or
                        ((bytes[25].toInt() and 0xFF) shl 8) or
                        ((bytes[26].toInt() and 0xFF) shl 16) or
                        ((bytes[27].toInt() and 0xFF) shl 24)

                var dataOffset = -1
                var i = 12
                while (i < bytes.size - 8) {
                    if (bytes[i] == 'd'.code.toByte() &&
                        bytes[i + 1] == 'a'.code.toByte() &&
                        bytes[i + 2] == 't'.code.toByte() &&
                        bytes[i + 3] == 'a'.code.toByte()
                    ) {
                        dataOffset = i + 8
                        break
                    }
                    i++
                }
                if (dataOffset < 0 || dataOffset >= bytes.size) {
                    dataOffset = 44
                }
                val numShorts = (bytes.size - dataOffset) / 2
                val shorts = ShortArray(numShorts)
                val buffer = ByteBuffer.wrap(bytes, dataOffset, numShorts * 2).order(ByteOrder.LITTLE_ENDIAN)
                buffer.asShortBuffer().get(shorts)
                PcmAudio(shorts, if (sampleRate in 8000..96000) sampleRate else 44100)
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Plays the appropriate sound effect when the ball hits a peg.
     * Pitch climbs dynamically as hitCountInShot increases, recreating Peggle's musical chimes.
     */
    fun playPegHit(hitCountInShot: Int, pegType: PegType = PegType.BLUE) {
        if (!soundEnabled) return

        // Pitch climbs up smoothly per hit (1.0x up to 2.0x)
        val rate = (1.0f + (hitCountInShot - 1) * 0.055f).coerceIn(0.85f, 2.0f)

        when (pegType) {
            PegType.BLUE -> {
                playPcm(pcmPegHit, volume = 0.85f, rate = rate)
            }
            PegType.ORANGE -> {
                playPcm(pcmOrangePeg, volume = 1.0f, rate = rate)
                playPcm(pcmKofFlame, volume = 0.35f, rate = 1.1f)
            }
            PegType.GREEN -> {
                playPcm(pcmGreenPeg, volume = 1.0f, rate = 1.0f)
                playPcm(pcmKofSuperFlash, volume = 0.8f, rate = 1.0f)
            }
            PegType.PURPLE -> {
                playPcm(pcmPurplePeg, volume = 0.95f, rate = rate * 1.05f)
            }
            PegType.MULTIBALL -> {
                playPcm(pcmPegHit, volume = 1.0f, rate = rate * 1.25f)
                playPcm(pcmKofSuperFlash, volume = 0.7f, rate = 1.2f)
            }
            PegType.LONG_AIM -> {
                playPcm(pcmPegHit, volume = 1.0f, rate = rate * 1.15f)
                playPcm(pcmKofSuperFlash, volume = 0.6f, rate = 1.0f)
            }
            PegType.BLAST_RADIUS -> {
                playPcm(pcmPegHit, volume = 1.0f, rate = rate)
                playPcm(pcmKofHeavyImpact, volume = 0.75f, rate = 1.0f)
            }
        }
    }

    /**
     * Plays the celebratory sound effect when the ball enters the free-ball bucket.
     */
    fun playBucketCatch() {
        if (!soundEnabled) return
        playPcm(pcmBucketCatch, volume = 1.0f, rate = 1.0f)
        scope.launch {
            delay(100)
            playPcm(pcmKofReadyGo, volume = 0.4f, rate = 1.2f)
        }
    }

    /**
     * Plays cannon shot launch sound.
     */
    fun playCannonFire() {
        if (!soundEnabled) return
        val randomPitch = 0.95f + Random.nextFloat() * 0.10f
        playPcm(pcmCannonShot, volume = 0.9f, rate = randomPitch)
    }

    /**
     * Plays sound when lit pegs pop during resolving phase.
     */
    fun playPegPop() {
        if (!soundEnabled) return
        val randomPitch = 0.9f + Random.nextFloat() * 0.2f
        playPcm(pcmPegPop, volume = 0.7f, rate = randomPitch)
    }

    /**
     * Plays KOF Neo Geo style heavy strike impact on high combo (10+ pegs).
     */
    fun playKofHeavyImpact() {
        if (!soundEnabled) return
        playPcm(pcmKofHeavyImpact, volume = 0.95f, rate = 1.0f)
    }

    /**
     * Plays KOF fiery burst on flame powers and fever slow-mo.
     */
    fun playKofFlameBurst() {
        if (!soundEnabled) return
        playPcm(pcmKofFlame, volume = 0.9f, rate = 1.0f)
    }

    /**
     * Plays KOF MAX Mode / Super flash activation.
     */
    fun playKofSuperFlash() {
        if (!soundEnabled) return
        playPcm(pcmKofSuperFlash, volume = 1.0f, rate = 1.0f)
    }

    /**
     * Plays KOF arcade round start cue.
     */
    fun playKofReadyGo() {
        if (!soundEnabled) return
        playPcm(pcmKofReadyGo, volume = 0.85f, rate = 1.0f)
    }

    /**
     * Plays KOF K.O. Victory gong and celebratory sting.
     */
    fun playKofKoVictory() {
        if (!soundEnabled) return
        playPcm(pcmKofKo, volume = 1.0f, rate = 1.0f)
    }

    /**
     * Plays Extreme Fever Ode to Joy fanfare.
     */
    fun playExtremeFeverOdeToJoy() {
        if (!soundEnabled) return
        stopFeverMelody()
        feverMelodyJob = scope.launch {
            playPcm(pcmKofKo, volume = 0.9f, rate = 1.0f)
            delay(400)

            for (note in odeToJoyMelody) {
                if (!isActive || !soundEnabled) break
                val notePitch = (note.first / 523.25f).coerceIn(0.5f, 2.0f)
                playPcm(pcmOrangePeg, volume = 0.85f, rate = notePitch)
                delay(note.second)
            }
        }
    }

    fun stopFeverMelody() {
        feverMelodyJob?.cancel()
        feverMelodyJob = null
    }

    private fun playPcm(pcm: PcmAudio?, volume: Float, rate: Float) {
        if (!soundEnabled || pcm == null || pcm.samples.isEmpty()) return
        scope.launch {
            try {
                val effectiveRate = (pcm.sampleRate * rate).toInt().coerceIn(8000, 96000)
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(effectiveRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcm.samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                if (volume < 0.98f) {
                    val scaled = ShortArray(pcm.samples.size) { idx ->
                        (pcm.samples[idx] * volume).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                    track.write(scaled, 0, scaled.size)
                } else {
                    track.write(pcm.samples, 0, pcm.samples.size)
                }
                track.play()

                val durationMs = (pcm.samples.size.toDouble() / effectiveRate * 1000).toLong() + 50L
                delay(durationMs)
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            } catch (_: Exception) {}
        }
    }

    fun release() {
        stopFeverMelody()
    }

    companion object {
        @Volatile
        private var INSTANCE: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SoundManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
