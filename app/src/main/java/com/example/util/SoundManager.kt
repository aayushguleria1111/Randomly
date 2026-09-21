package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class SoundType {
    TOOL_CLICK,
    FAVORITE,
    UNFAVORITE,
    TOOL_USE,
    SETTING_CHANGE,
    SUCCESS_CHIME,
    TAB_SWITCH,
    ACTION_TICK,
    DELETE_CLEAR,
    ERROR_BUZZ
}

object SoundManager {
    private const val TAG = "SoundManager"
    private const val SAMPLE_RATE = 44100

    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<SoundType, Int>()
    @Volatile
    private var isInitialized = false

    @Synchronized
    fun initialize(context: Context) {
        if (isInitialized) return

        try {
            val pool = SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .build()
            soundPool = pool

            val soundDir = File(context.cacheDir, "app_sounds").apply { mkdirs() }

            SoundType.values().forEach { soundType ->
                val file = File(soundDir, "${soundType.name.lowercase()}.wav")
                if (!file.exists() || file.length() == 0L) {
                    val pcmSamples = generateSamples(soundType)
                    val wavBytes = createWavFileBytes(pcmSamples, SAMPLE_RATE)
                    FileOutputStream(file).use { it.write(wavBytes) }
                }
                val id = pool.load(file.absolutePath, 1)
                soundIds[soundType] = id
            }

            isInitialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SoundPool", e)
        }
    }

    fun play(context: Context, soundType: SoundType, enabled: Boolean = true, volume: Float = 0.85f) {
        if (!enabled) return
        if (!isInitialized) {
            initialize(context.applicationContext)
        }
        val pool = soundPool ?: return
        val id = soundIds[soundType] ?: return
        try {
            pool.play(id, volume, volume, 1, 0, 1.0f)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play sound: $soundType", e)
        }
    }

    fun playToolClick(context: Context, enabled: Boolean) = play(context, SoundType.TOOL_CLICK, enabled, 0.85f)
    fun playFavorite(context: Context, enabled: Boolean) = play(context, SoundType.FAVORITE, enabled, 0.95f)
    fun playUnfavorite(context: Context, enabled: Boolean) = play(context, SoundType.UNFAVORITE, enabled, 0.8f)
    fun playToolUse(context: Context, enabled: Boolean) = play(context, SoundType.TOOL_USE, enabled, 0.9f)
    fun playSettingChange(context: Context, enabled: Boolean) = play(context, SoundType.SETTING_CHANGE, enabled, 0.75f)
    fun playSuccessChime(context: Context, enabled: Boolean) = play(context, SoundType.SUCCESS_CHIME, enabled, 0.95f)
    fun playTabSwitch(context: Context, enabled: Boolean) = play(context, SoundType.TAB_SWITCH, enabled, 0.7f)
    fun playActionTick(context: Context, enabled: Boolean) = play(context, SoundType.ACTION_TICK, enabled, 0.75f)
    fun playDeleteClear(context: Context, enabled: Boolean) = play(context, SoundType.DELETE_CLEAR, enabled, 0.8f)
    fun playErrorBuzz(context: Context, enabled: Boolean) = play(context, SoundType.ERROR_BUZZ, enabled, 0.85f)

    private fun generateSamples(type: SoundType): ShortArray {
        return when (type) {
            SoundType.TOOL_CLICK -> {
                // Modern, bubbly pop/blip sweeping 420Hz -> 760Hz, 38ms
                val durationMs = 38
                val totalSamples = (SAMPLE_RATE * durationMs / 1000)
                ShortArray(totalSamples) { i ->
                    val t = i.toDouble() / totalSamples
                    val freq = 420.0 + (760.0 - 420.0) * t
                    val phase = 2.0 * PI * freq * (i.toDouble() / SAMPLE_RATE)
                    val env = if (i < totalSamples * 0.1) {
                        i / (totalSamples * 0.1)
                    } else {
                        exp(-6.5 * t)
                    }
                    (sin(phase) * env * 22000).toInt().toShort()
                }
            }

            SoundType.FAVORITE -> {
                // Ascending 2-tone melodic chime: Note 1 (D5 ~587Hz) for 50ms, Note 2 (A5 ~880Hz) for 90ms
                val durationMs = 140
                val totalSamples = (SAMPLE_RATE * durationMs / 1000)
                val splitSample = (SAMPLE_RATE * 50 / 1000)
                ShortArray(totalSamples) { i ->
                    val (freq, localT) = if (i < splitSample) {
                        587.33 to (i.toDouble() / splitSample)
                    } else {
                        880.0 to ((i - splitSample).toDouble() / (totalSamples - splitSample))
                    }
                    val phase = 2.0 * PI * freq * (i.toDouble() / SAMPLE_RATE)
                    val attack = if (localT < 0.1) localT / 0.1 else 1.0
                    val decay = exp(-3.2 * localT)
                    val env = attack * decay
                    (sin(phase) * env * 24000).toInt().toShort()
                }
            }

            SoundType.UNFAVORITE -> {
                // Descending 2-tone soft note: Note 1 (E5 ~659Hz) for 45ms, Note 2 (A4 ~440Hz) for 75ms
                val durationMs = 120
                val totalSamples = (SAMPLE_RATE * durationMs / 1000)
                val splitSample = (SAMPLE_RATE * 45 / 1000)
                ShortArray(totalSamples) { i ->
                    val (freq, localT) = if (i < splitSample) {
                        659.25 to (i.toDouble() / splitSample)
                    } else {
                        440.0 to ((i - splitSample).toDouble() / (totalSamples - splitSample))
                    }
                    val phase = 2.0 * PI * freq * (i.toDouble() / SAMPLE_RATE)
                    val attack = if (localT < 0.12) localT / 0.12 else 1.0
                    val decay = exp(-4.5 * localT)
                    val env = attack * decay
                    (sin(phase) * env * 19000).toInt().toShort()
                }
            }

            SoundType.TOOL_USE -> {
                // Punchy mechanical snap / roll trigger (pitch drop from 340Hz down to 130Hz, 65ms)
                val durationMs = 65
                val totalSamples = (SAMPLE_RATE * durationMs / 1000)
                ShortArray(totalSamples) { i ->
                    val t = i.toDouble() / totalSamples
                    val freq = 340.0 - (340.0 - 130.0) * (t * t)
                    val phase = 2.0 * PI * freq * (i.toDouble() / SAMPLE_RATE)
                    val subHarmonic = 0.3 * sin(2.0 * PI * (freq * 0.5) * (i.toDouble() / SAMPLE_RATE))
                    val env = if (t < 0.08) t / 0.08 else exp(-5.0 * t)
                    ((sin(phase) + subHarmonic) * env * 23000).toInt().toShort()
                }
            }

            SoundType.SETTING_CHANGE -> {
                // Crisp high-frequency metallic tick 1250Hz, 28ms
                val durationMs = 28
                val totalSamples = (SAMPLE_RATE * durationMs / 1000)
                ShortArray(totalSamples) { i ->
                    val t = i.toDouble() / totalSamples
                    val freq = 1250.0 - 200.0 * t
                    val phase = 2.0 * PI * freq * (i.toDouble() / SAMPLE_RATE)
                    val env = if (t < 0.05) t / 0.05 else exp(-9.0 * t)
                    (sin(phase) * env * 21000).toInt().toShort()
                }
            }

            SoundType.SUCCESS_CHIME -> {
                // Sparkling harmonic chord (C6 1046.5Hz + E6 1318.5Hz + subtle G6 1567.9Hz), 180ms
                val durationMs = 180
                val totalSamples = (SAMPLE_RATE * durationMs / 1000)
                ShortArray(totalSamples) { i ->
                    val t = i.toDouble() / totalSamples
                    val p1 = 2.0 * PI * 1046.5 * (i.toDouble() / SAMPLE_RATE)
                    val p2 = 2.0 * PI * 1318.5 * (i.toDouble() / SAMPLE_RATE)
                    val p3 = 2.0 * PI * 1567.9 * (i.toDouble() / SAMPLE_RATE)
                    val sample = sin(p1) * 0.5 + sin(p2) * 0.35 + sin(p3) * 0.15
                    val attack = if (t < 0.06) t / 0.06 else 1.0
                    val decay = exp(-3.8 * t)
                    (sample * attack * decay * 25000).toInt().toShort()
                }
            }

            SoundType.TAB_SWITCH -> {
                // Subtle airy wood tap 540Hz, 22ms
                val durationMs = 22
                val totalSamples = (SAMPLE_RATE * durationMs / 1000)
                ShortArray(totalSamples) { i ->
                    val t = i.toDouble() / totalSamples
                    val freq = 540.0 - 150.0 * t
                    val phase = 2.0 * PI * freq * (i.toDouble() / SAMPLE_RATE)
                    val env = if (t < 0.08) t / 0.08 else exp(-8.0 * t)
                    (sin(phase) * env * 17000).toInt().toShort()
                }
            }

            SoundType.ACTION_TICK -> {
                // Crisp click blip 780Hz, 24ms
                val durationMs = 24
                val totalSamples = (SAMPLE_RATE * durationMs / 1000)
                ShortArray(totalSamples) { i ->
                    val t = i.toDouble() / totalSamples
                    val phase = 2.0 * PI * 780.0 * (i.toDouble() / SAMPLE_RATE)
                    val env = if (t < 0.06) t / 0.06 else exp(-7.5 * t)
                    (sin(phase) * env * 18000).toInt().toShort()
                }
            }

            SoundType.DELETE_CLEAR -> {
                // Downward smooth swoosh 480Hz -> 190Hz, 95ms
                val durationMs = 95
                val totalSamples = (SAMPLE_RATE * durationMs / 1000)
                ShortArray(totalSamples) { i ->
                    val t = i.toDouble() / totalSamples
                    val freq = 480.0 - 290.0 * t
                    val phase = 2.0 * PI * freq * (i.toDouble() / SAMPLE_RATE)
                    val env = exp(-4.5 * t)
                    (sin(phase) * env * 20000).toInt().toShort()
                }
            }

            SoundType.ERROR_BUZZ -> {
                // Dual low warning buzz: 220Hz, 120ms
                val durationMs = 120
                val totalSamples = (SAMPLE_RATE * durationMs / 1000)
                ShortArray(totalSamples) { i ->
                    val t = i.toDouble() / totalSamples
                    val phase = 2.0 * PI * 220.0 * (i.toDouble() / SAMPLE_RATE)
                    val inPulse = (t in 0.0..0.38) || (t in 0.55..0.92)
                    val env = if (inPulse) 0.85 else 0.0
                    (sin(phase) * env * 22000).toInt().toShort()
                }
            }
        }
    }

    private fun createWavFileBytes(samples: ShortArray, sampleRate: Int): ByteArray {
        val numChannels = 1
        val bitsPerSample = 16
        val byteRate = sampleRate * numChannels * (bitsPerSample / 8)
        val blockAlign = numChannels * (bitsPerSample / 8)
        val dataSize = samples.size * 2
        val chunkSize = 36 + dataSize

        val buffer = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)

        // RIFF chunk
        buffer.put('R'.code.toByte())
        buffer.put('I'.code.toByte())
        buffer.put('F'.code.toByte())
        buffer.put('F'.code.toByte())
        buffer.putInt(chunkSize)
        buffer.put('W'.code.toByte())
        buffer.put('A'.code.toByte())
        buffer.put('V'.code.toByte())
        buffer.put('E'.code.toByte())

        // fmt chunk
        buffer.put('f'.code.toByte())
        buffer.put('m'.code.toByte())
        buffer.put('t'.code.toByte())
        buffer.put(' '.code.toByte())
        buffer.putInt(16) // Subchunk1Size for PCM
        buffer.putShort(1) // AudioFormat 1 = PCM
        buffer.putShort(numChannels.toShort())
        buffer.putInt(sampleRate)
        buffer.putInt(byteRate)
        buffer.putShort(blockAlign.toShort())
        buffer.putShort(bitsPerSample.toShort())

        // data chunk
        buffer.put('d'.code.toByte())
        buffer.put('a'.code.toByte())
        buffer.put('t'.code.toByte())
        buffer.put('a'.code.toByte())
        buffer.putInt(dataSize)

        for (sample in samples) {
            buffer.putShort(sample)
        }

        return buffer.array()
    }
}
