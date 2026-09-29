package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import java.io.File

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
    private var toneGenerator: ToneGenerator? = null
    @Volatile
    private var isInitialized = false

    @Synchronized
    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            // Clean up any legacy sound cache if present
            val soundDir = File(context.cacheDir, "app_sounds")
            if (soundDir.exists()) {
                soundDir.deleteRecursively()
            }

            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            isInitialized = true
        } catch (e: Exception) {
            Log.w(TAG, "ToneGenerator initialization warning (fallback to AudioManager): ${e.message}")
        }
    }

    fun play(context: Context, soundType: SoundType, enabled: Boolean = true, volume: Float = 0.85f) {
        if (!enabled) return
        if (!isInitialized) {
            initialize(context.applicationContext)
        }
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

        try {
            when (soundType) {
                SoundType.TOOL_CLICK -> {
                    audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, volume)
                }
                SoundType.FAVORITE -> {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 70)
                        ?: audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, volume)
                }
                SoundType.UNFAVORITE -> {
                    audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, volume * 0.7f)
                }
                SoundType.TOOL_USE -> {
                    audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, volume)
                }
                SoundType.SETTING_CHANGE -> {
                    audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, volume * 0.6f)
                }
                SoundType.SUCCESS_CHIME -> {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 100)
                        ?: audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, volume)
                }
                SoundType.TAB_SWITCH -> {
                    audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, volume * 0.5f)
                }
                SoundType.ACTION_TICK -> {
                    audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, volume * 0.6f)
                }
                SoundType.DELETE_CLEAR -> {
                    audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE, volume)
                }
                SoundType.ERROR_BUZZ -> {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 120)
                        ?: audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE, volume)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Audio playback error for $soundType: ${e.message}")
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
}
