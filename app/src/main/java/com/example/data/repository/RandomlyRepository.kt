package com.example.data.repository

import com.example.data.local.DataStoreManager
import com.example.data.local.FavoritesDao
import com.example.data.local.HistoryDao
import com.example.data.local.SavedListDao
import com.example.data.local.ToolPresetDao
import com.example.data.local.ToolUsageDao
import com.example.data.model.AppColorTheme
import com.example.data.model.AppSettings
import com.example.data.model.AppThemeMode
import com.example.data.model.FavoriteItem
import com.example.data.model.HistoryItem
import com.example.data.model.SavedList
import com.example.data.model.ToolPreset
import com.example.data.model.ToolUsage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class RandomlyRepository(
    private val historyDao: HistoryDao,
    private val favoritesDao: FavoritesDao,
    private val savedListDao: SavedListDao,
    private val toolUsageDao: ToolUsageDao,
    private val toolPresetDao: ToolPresetDao,
    private val dataStoreManager: DataStoreManager
) {
    val allHistory: Flow<List<HistoryItem>> = historyDao.getAllHistory()
    val allFavorites: Flow<List<FavoriteItem>> = favoritesDao.getAllFavorites()
    val allSavedLists: Flow<List<SavedList>> = savedListDao.getAllSavedLists()
    val mostUsedTools: Flow<List<ToolUsage>> = toolUsageDao.getMostUsedTools()
    val settings: Flow<AppSettings> = dataStoreManager.settingsFlow

    fun getHistoryForTool(toolType: String, limit: Int = 10): Flow<List<HistoryItem>> {
        return historyDao.getHistoryForTool(toolType, limit)
    }

    fun getPresetsForTool(toolId: String): Flow<List<ToolPreset>> {
        return toolPresetDao.getPresetsForTool(toolId)
    }

    suspend fun recordResult(
        toolType: String,
        title: String,
        result: String,
        details: String = ""
    ) {
        val currentSettings = settings.first()
        if (currentSettings.saveHistoryEnabled) {
            historyDao.insertHistory(
                HistoryItem(
                    toolType = toolType,
                    title = title,
                    result = result,
                    details = details,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        // Also track usage when a tool is executed
        toolUsageDao.incrementUsage(toolType)
    }

    suspend fun recordToolOpen(toolId: String) {
        toolUsageDao.incrementUsage(toolId)
    }

    suspend fun savePreset(toolId: String, name: String, items: List<String>): Long {
        val preset = ToolPreset.create(toolId = toolId, name = name, items = items, isBuiltIn = false)
        return toolPresetDao.insertPreset(preset)
    }

    suspend fun deletePreset(id: Long) {
        toolPresetDao.deletePresetById(id)
    }

    suspend fun deleteHistoryItem(id: Long) {
        historyDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        historyDao.clearAllHistory()
    }

    fun isFavorite(toolId: String): Flow<Boolean> {
        return favoritesDao.isFavorite(toolId)
    }

    suspend fun toggleFavorite(toolId: String, isFav: Boolean) {
        if (isFav) {
            favoritesDao.removeFavorite(toolId)
        } else {
            favoritesDao.addFavorite(FavoriteItem(toolId = toolId))
        }
    }

    suspend fun saveList(name: String, items: List<String>, id: Long = 0): Long {
        val list = SavedList.fromList(name = name, items = items, id = id)
        return if (id == 0L) {
            savedListDao.insertSavedList(list)
        } else {
            savedListDao.updateSavedList(list)
            id
        }
    }

    suspend fun deleteSavedList(id: Long) {
        savedListDao.deleteSavedListById(id)
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        dataStoreManager.setThemeMode(mode)
    }

    suspend fun setColorTheme(colorTheme: AppColorTheme) {
        dataStoreManager.setColorTheme(colorTheme)
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStoreManager.setHapticsEnabled(enabled)
    }

    suspend fun setAnimationsEnabled(enabled: Boolean) {
        dataStoreManager.setAnimationsEnabled(enabled)
    }

    suspend fun setSaveHistoryEnabled(enabled: Boolean) {
        dataStoreManager.setSaveHistoryEnabled(enabled)
    }

    suspend fun setDefaultNumberRange(min: Int, max: Int) {
        dataStoreManager.setDefaultNumberRange(min, max)
    }

    suspend fun setDefaultDiceType(diceType: String) {
        dataStoreManager.setDefaultDiceType(diceType)
    }
}
