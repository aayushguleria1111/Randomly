package com.example.util

import android.content.Context

object AudioHapticFeedback {

    fun onToolClick(context: Context, soundEnabled: Boolean, hapticsEnabled: Boolean) {
        SoundManager.playToolClick(context, soundEnabled)
        HapticFeedbackUtil.performToolClick(context, hapticsEnabled)
    }

    fun onFavorite(context: Context, isFavoriteNow: Boolean, soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (isFavoriteNow) {
            SoundManager.playFavorite(context, soundEnabled)
            HapticFeedbackUtil.performFavorite(context, hapticsEnabled)
        } else {
            SoundManager.playUnfavorite(context, soundEnabled)
            HapticFeedbackUtil.performUnfavorite(context, hapticsEnabled)
        }
    }

    fun onToolUse(context: Context, soundEnabled: Boolean, hapticsEnabled: Boolean) {
        SoundManager.playToolUse(context, soundEnabled)
        HapticFeedbackUtil.performToolUse(context, hapticsEnabled)
    }

    fun onSettingChange(context: Context, soundEnabled: Boolean, hapticsEnabled: Boolean) {
        SoundManager.playSettingChange(context, soundEnabled)
        HapticFeedbackUtil.performSettingChange(context, hapticsEnabled)
    }

    fun onSuccess(context: Context, soundEnabled: Boolean, hapticsEnabled: Boolean) {
        SoundManager.playSuccessChime(context, soundEnabled)
        HapticFeedbackUtil.performSuccess(context, hapticsEnabled)
    }

    fun onTabSwitch(context: Context, soundEnabled: Boolean, hapticsEnabled: Boolean) {
        SoundManager.playTabSwitch(context, soundEnabled)
        HapticFeedbackUtil.performTick(context, hapticsEnabled)
    }

    fun onAction(context: Context, soundEnabled: Boolean, hapticsEnabled: Boolean) {
        SoundManager.playActionTick(context, soundEnabled)
        HapticFeedbackUtil.performTick(context, hapticsEnabled)
    }

    fun onDeleteClear(context: Context, soundEnabled: Boolean, hapticsEnabled: Boolean) {
        SoundManager.playDeleteClear(context, soundEnabled)
        HapticFeedbackUtil.performWarning(context, hapticsEnabled)
    }

    fun onError(context: Context, soundEnabled: Boolean, hapticsEnabled: Boolean) {
        SoundManager.playErrorBuzz(context, soundEnabled)
        HapticFeedbackUtil.performError(context, hapticsEnabled)
    }
}
