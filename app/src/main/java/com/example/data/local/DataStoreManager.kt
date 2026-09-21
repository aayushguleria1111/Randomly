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
import com.example.data.model.AppTextSize
import com.example.data.model.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "randomly_settings")

class DataStoreManager(private val context: Context) {

    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val COLOR_THEME = stringPreferencesKey("color_theme")
        val TEXT_SIZE = stringPreferencesKey("text_size")
        val SOUND_EFFECTS_ENABLED = booleanPreferencesKey("sound_effects_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val SAVE_HISTORY_ENABLED = booleanPreferencesKey("save_history_enabled")
        val DEFAULT_MIN_NUMBER = intPreferencesKey("default_min_number")
        val DEFAULT_MAX_NUMBER = intPreferencesKey("default_max_number")
        val DEFAULT_DICE_TYPE = stringPreferencesKey("default_dice_type")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val themeModeStr = preferences[THEME_MODE] ?: AppThemeMode.SYSTEM.name
        val themeMode = try {
            AppThemeMode.valueOf(themeModeStr)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }

        val colorThemeStr = preferences[COLOR_THEME] ?: AppColorTheme.INDIGO.name
        val colorTheme = try {
            AppColorTheme.valueOf(colorThemeStr)
        } catch (e: Exception) {
            AppColorTheme.INDIGO
        }

        val textSizeStr = preferences[TEXT_SIZE] ?: AppTextSize.SMALL_MEDIUM.name
        val textSize = try {
            AppTextSize.valueOf(textSizeStr)
        } catch (e: Exception) {
            AppTextSize.SMALL_MEDIUM
        }

        AppSettings(
            themeMode = themeMode,
            colorTheme = colorTheme,
            textSize = textSize,
            soundEffectsEnabled = preferences[SOUND_EFFECTS_ENABLED] ?: true,
            hapticsEnabled = preferences[HAPTICS_ENABLED] ?: true,
            animationsEnabled = preferences[ANIMATIONS_ENABLED] ?: true,
            saveHistoryEnabled = preferences[SAVE_HISTORY_ENABLED] ?: true,
            defaultMinNumber = preferences[DEFAULT_MIN_NUMBER] ?: 1,
            defaultMaxNumber = preferences[DEFAULT_MAX_NUMBER] ?: 100,
            defaultDiceType = preferences[DEFAULT_DICE_TYPE] ?: "d6"
        )
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode.name
        }
    }

    suspend fun setColorTheme(colorTheme: AppColorTheme) {
        context.dataStore.edit { preferences ->
            preferences[COLOR_THEME] = colorTheme.name
        }
    }

    suspend fun setTextSize(textSize: AppTextSize) {
        context.dataStore.edit { preferences ->
            preferences[TEXT_SIZE] = textSize.name
        }
    }

    suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SOUND_EFFECTS_ENABLED] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun setAnimationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ANIMATIONS_ENABLED] = enabled
        }
    }

    suspend fun setSaveHistoryEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SAVE_HISTORY_ENABLED] = enabled
        }
    }

    suspend fun setDefaultNumberRange(min: Int, max: Int) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_MIN_NUMBER] = min
            preferences[DEFAULT_MAX_NUMBER] = max
        }
    }

    suspend fun setDefaultDiceType(diceType: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_DICE_TYPE] = diceType
        }
    }

    fun getLastUsedItemsFlow(toolId: String, defaultItems: List<String>): Flow<List<String>> =
        context.dataStore.data.map { preferences ->
            val key = stringPreferencesKey("last_used_tool_items_$toolId")
            val raw = preferences[key]
            if (!raw.isNullOrBlank()) {
                raw.split("|||").filter { it.isNotBlank() }
            } else {
                defaultItems
            }
        }

    suspend fun saveLastUsedItems(toolId: String, items: List<String>) {
        val key = stringPreferencesKey("last_used_tool_items_$toolId")
        context.dataStore.edit { preferences ->
            preferences[key] = items.joinToString("|||")
        }
    }
}
