package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DataStoreManager
import com.example.data.model.AppColorTheme
import com.example.data.model.AppSettings
import com.example.data.model.AppThemeMode
import com.example.data.model.FavoriteItem
import com.example.data.model.HistoryItem
import com.example.data.model.SavedList
import com.example.data.model.ToolPreset
import com.example.data.model.ToolType
import com.example.data.model.ToolUsage
import com.example.data.repository.RandomlyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RandomlyViewModel(
    application: Application,
    private val repository: RandomlyRepository
) : AndroidViewModel(application) {

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppSettings()
        )

    val favorites: StateFlow<List<FavoriteItem>> = repository.allFavorites
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val history: StateFlow<List<HistoryItem>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savedLists: StateFlow<List<SavedList>> = repository.allSavedLists
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val mostUsedTools: StateFlow<List<ToolUsage>> = repository.mostUsedTools
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    fun getHistoryForTool(toolId: String, limit: Int = 5): Flow<List<HistoryItem>> {
        return repository.getHistoryForTool(toolId, limit)
    }

    fun getPresetsForTool(toolId: String): Flow<List<ToolPreset>> {
        return repository.getPresetsForTool(toolId)
    }

    fun recordToolOpen(toolId: String) {
        viewModelScope.launch {
            repository.recordToolOpen(toolId)
        }
    }

    fun showMessage(message: String) {
        viewModelScope.launch {
            _snackbarMessage.emit(message)
        }
    }

    fun recordResult(toolType: ToolType, title: String, result: String, details: String = "") {
        viewModelScope.launch {
            repository.recordResult(
                toolType = toolType.id,
                title = title,
                result = result,
                details = details
            )
        }
    }

    fun toggleFavorite(toolType: ToolType) {
        viewModelScope.launch {
            val isFav = favorites.value.any { it.toolId == toolType.id }
            repository.toggleFavorite(toolType.id, isFav)
            val msg = if (isFav) "Removed ${toolType.title} from favorites" else "Added ${toolType.title} to favorites"
            _snackbarMessage.emit(msg)
        }
    }

    fun isToolFavorite(toolId: String): Boolean {
        return favorites.value.any { it.toolId == toolId }
    }

    fun savePreset(toolId: String, name: String, items: List<String>) {
        viewModelScope.launch {
            repository.savePreset(toolId, name, items)
            _snackbarMessage.emit("Preset '$name' saved!")
        }
    }

    fun deletePreset(id: Long) {
        viewModelScope.launch {
            repository.deletePreset(id)
            _snackbarMessage.emit("Preset deleted")
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id)
            _snackbarMessage.emit("History item deleted")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _snackbarMessage.emit("History cleared")
        }
    }

    fun saveCustomList(name: String, items: List<String>, id: Long = 0) {
        viewModelScope.launch {
            repository.saveList(name, items, id)
            _snackbarMessage.emit("List '$name' saved successfully")
        }
    }

    fun deleteCustomList(id: Long) {
        viewModelScope.launch {
            repository.deleteSavedList(id)
            _snackbarMessage.emit("List deleted")
        }
    }

    fun updateThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun updateColorTheme(colorTheme: AppColorTheme) {
        viewModelScope.launch {
            repository.setColorTheme(colorTheme)
        }
    }

    fun updateHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setHapticsEnabled(enabled)
        }
    }

    fun updateAnimationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAnimationsEnabled(enabled)
        }
    }

    fun updateSaveHistoryEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSaveHistoryEnabled(enabled)
        }
    }

    fun updateDefaultNumberRange(min: Int, max: Int) {
        viewModelScope.launch {
            repository.setDefaultNumberRange(min, max)
        }
    }

    fun updateDefaultDiceType(diceType: String) {
        viewModelScope.launch {
            repository.setDefaultDiceType(diceType)
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getDatabase(application)
                    val dataStore = DataStoreManager(application)
                    val repo = RandomlyRepository(
                        historyDao = db.historyDao(),
                        favoritesDao = db.favoritesDao(),
                        savedListDao = db.savedListDao(),
                        toolUsageDao = db.toolUsageDao(),
                        toolPresetDao = db.toolPresetDao(),
                        dataStoreManager = dataStore
                    )
                    return RandomlyViewModel(application, repo) as T
                }
            }
    }
}
