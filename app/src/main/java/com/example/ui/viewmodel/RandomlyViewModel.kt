package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DataStoreManager
import com.example.data.model.AppColorTheme
import com.example.data.model.AppSettings
import com.example.data.model.AppTextSize
import com.example.data.model.AppThemeMode
import com.example.data.model.FavoriteItem
import com.example.data.model.HistoryItem
import com.example.data.model.SavedList
import com.example.data.model.ToolPreset
import com.example.data.model.ToolType
import com.example.data.model.ToolUsage
import com.example.data.repository.RandomlyRepository
import com.example.util.AudioHapticFeedback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RandomlyViewModel(
    application: Application,
    private val repository: RandomlyRepository
) : AndroidViewModel(application) {

    var hasShownSplash: Boolean = false

    init {
        viewModelScope.launch {
            repository.ensureCleanPresets()
        }
    }

    val settings: StateFlow<AppSettings> = repository.settings.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        AppSettings()
    )

    val favorites: StateFlow<List<FavoriteItem>> = repository.allFavorites.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val history: StateFlow<List<HistoryItem>> = repository.allHistory.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val mostUsedTools: StateFlow<List<ToolUsage>> = repository.mostUsedTools.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allSavedLists: StateFlow<List<SavedList>> = repository.allSavedLists.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    @Composable
    fun isToolFavorite(toolId: String): Boolean {
        val favs by favorites.collectAsState()
        return favs.any { it.toolId == toolId }
    }

    fun isFavoriteSync(toolId: String): Boolean {
        return favorites.value.any { it.toolId == toolId }
    }

    fun toggleFavorite(tool: ToolType) {
        val isFav = favorites.value.any { it.toolId == tool.id }
        val currentSettings = settings.value
        val context = getApplication<Application>()
        AudioHapticFeedback.onFavorite(
            context = context,
            isFavoriteNow = !isFav,
            soundEnabled = currentSettings.soundEffectsEnabled,
            hapticsEnabled = currentSettings.hapticsEnabled
        )
        viewModelScope.launch {
            repository.toggleFavorite(tool.id, isFav)
            val msg = if (isFav) "Removed ${tool.title} from favorites" else "Added ${tool.title} to favorites"
            _snackbarMessage.emit(msg)
        }
    }

    fun recordResult(
        toolType: ToolType,
        title: String,
        result: String,
        details: String = ""
    ) {
        viewModelScope.launch {
            repository.recordResult(
                toolType = toolType.id,
                title = title,
                result = result,
                details = details
            )
        }
    }

    fun recordToolOpen(toolId: String) {
        viewModelScope.launch {
            repository.recordToolOpen(toolId)
        }
    }

    fun getPresetsForTool(toolId: String): Flow<List<ToolPreset>> {
        return repository.getPresetsForTool(toolId).map { list ->
            val builtIn = list.filter { it.isBuiltIn }.distinctBy { it.presetName }.take(3)
            val custom = list.filter { !it.isBuiltIn }.distinctBy { it.id }
            builtIn + custom
        }
    }

    fun savePreset(toolId: String, name: String, items: List<String>) {
        viewModelScope.launch {
            repository.savePreset(toolId, name, items)
            _snackbarMessage.emit("Preset '$name' saved")
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
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _snackbarMessage.emit("History cleared")
        }
    }

    fun saveList(name: String, items: List<String>, id: Long = 0) {
        viewModelScope.launch {
            repository.saveList(name, items, id)
            _snackbarMessage.emit("List '$name' saved")
        }
    }

    fun deleteSavedList(id: Long) {
        viewModelScope.launch {
            repository.deleteSavedList(id)
            _snackbarMessage.emit("List deleted")
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setColorTheme(colorTheme: AppColorTheme) {
        viewModelScope.launch {
            repository.setColorTheme(colorTheme)
        }
    }

    fun setTextSize(textSize: AppTextSize) {
        viewModelScope.launch {
            repository.setTextSize(textSize)
        }
    }

    fun setSoundEffectsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSoundEffectsEnabled(enabled)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setHapticsEnabled(enabled)
        }
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAnimationsEnabled(enabled)
        }
    }

    fun setSaveHistoryEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSaveHistoryEnabled(enabled)
        }
    }

    fun setDefaultNumberRange(min: Int, max: Int) {
        viewModelScope.launch {
            repository.setDefaultNumberRange(min, max)
        }
    }

    fun setDefaultDiceType(diceType: String) {
        viewModelScope.launch {
            repository.setDefaultDiceType(diceType)
        }
    }

    fun getLastUsedItems(toolId: String, defaultItems: List<String>): Flow<List<String>> {
        return repository.getLastUsedItems(toolId, defaultItems)
    }

    fun saveLastUsedItems(toolId: String, items: List<String>) {
        viewModelScope.launch {
            repository.saveLastUsedItems(toolId, items)
        }
    }

    fun showMessage(message: String) {
        viewModelScope.launch {
            _snackbarMessage.emit(message)
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getDatabase(application)
                    val dataStoreManager = DataStoreManager(application)
                    val repository = RandomlyRepository(
                        historyDao = db.historyDao(),
                        favoritesDao = db.favoritesDao(),
                        savedListDao = db.savedListDao(),
                        toolUsageDao = db.toolUsageDao(),
                        toolPresetDao = db.toolPresetDao(),
                        dataStoreManager = dataStoreManager
                    )
                    return RandomlyViewModel(application, repository) as T
                }
            }
    }
}
