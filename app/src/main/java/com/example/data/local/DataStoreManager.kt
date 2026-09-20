package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppColorTheme
import com.example.data.model.AppSettings
import com.example.data.model.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "randomly_settings")

class DataStoreManager(private val context: Context) {

    private val KEY_THEME = stringPreferencesKey("theme_mode")
    private val KEY_COLOR_THEME = stringPreferencesKey("color_theme")
    private val KEY_HAPTICS = booleanPreferencesKey("haptics_enabled")
    private val KEY_ANIMATIONS = booleanPreferencesKey("animations_enabled")
    private val KEY_SAVE_HISTORY = booleanPreferencesKey("save_history_enabled")
    private val KEY_DEFAULT_MIN = intPreferencesKey("default_min_number")
    private val KEY_DEFAULT_MAX = intPreferencesKey("default_max_number")
    private val KEY_DEFAULT_DICE = stringPreferencesKey("default_dice_type")

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val themeStr = preferences[KEY_THEME] ?: AppThemeMode.SYSTEM.name
        val themeMode = try {
            AppThemeMode.valueOf(themeStr)
        } catch (_: Exception) {
            AppThemeMode.SYSTEM
        }

        val colorThemeStr = preferences[KEY_COLOR_THEME] ?: AppColorTheme.INDIGO.name
        val colorTheme = try {
            AppColorTheme.valueOf(colorThemeStr)
        } catch (_: Exception) {
            AppColorTheme.INDIGO
        }

        AppSettings(
            themeMode = themeMode,
            colorTheme = colorTheme,
            hapticsEnabled = preferences[KEY_HAPTICS] ?: true,
            animationsEnabled = preferences[KEY_ANIMATIONS] ?: true,
            saveHistoryEnabled = preferences[KEY_SAVE_HISTORY] ?: true,
            defaultMinNumber = preferences[KEY_DEFAULT_MIN] ?: 1,
            defaultMaxNumber = preferences[KEY_DEFAULT_MAX] ?: 100,
            defaultDiceType = preferences[KEY_DEFAULT_DICE] ?: "d6"
        )
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME] = mode.name
        }
    }

    suspend fun setColorTheme(colorTheme: AppColorTheme) {
        context.dataStore.edit { preferences ->
            preferences[KEY_COLOR_THEME] = colorTheme.name
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HAPTICS] = enabled
        }
    }

    suspend fun setAnimationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ANIMATIONS] = enabled
        }
    }

    suspend fun setSaveHistoryEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SAVE_HISTORY] = enabled
        }
    }

    suspend fun setDefaultNumberRange(min: Int, max: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DEFAULT_MIN] = min
            preferences[KEY_DEFAULT_MAX] = max
        }
    }

    suspend fun setDefaultDiceType(diceType: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DEFAULT_DICE] = diceType
        }
    }
}
