package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.FavoriteItem
import com.example.data.model.HistoryItem
import com.example.data.model.SavedList
import com.example.data.model.ToolPreset
import com.example.data.model.ToolUsage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        HistoryItem::class,
        FavoriteItem::class,
        SavedList::class,
        ToolUsage::class,
        ToolPreset::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun favoritesDao(): FavoritesDao
    abstract fun savedListDao(): SavedListDao
    abstract fun toolUsageDao(): ToolUsageDao
    abstract fun toolPresetDao(): ToolPresetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "randomly_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDefaultPresets(database.toolPresetDao())
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val wheelCount = database.toolPresetDao().getBuiltInPresetCountForTool("spin_wheel")
                        val listCount = database.toolPresetDao().getBuiltInPresetCountForTool("list_picker")
                        val choiceCount = database.toolPresetDao().getBuiltInPresetCountForTool("choice")
                        // If any tool does not have exactly 3 built-in presets or duplicates exist, re-seed
                        if (wheelCount != 3 || listCount != 3 || choiceCount != 3) {
                            seedDefaultPresets(database.toolPresetDao())
                        }
                    }
                }
            }

            private suspend fun seedDefaultPresets(presetDao: ToolPresetDao) {
                // Remove existing built-in presets to avoid duplication
                presetDao.deleteBuiltInPresets()

                val defaults = listOf(
                    // Exactly 3 Spin Wheel presets
                    ToolPreset.create("spin_wheel", "Food & Meals", listOf("Pizza", "Burger", "Sushi", "Tacos", "Pasta", "Salad"), isBuiltIn = true),
                    ToolPreset.create("spin_wheel", "Yes / No / Maybe", listOf("Yes", "No", "Maybe", "Definitely", "Ask Later"), isBuiltIn = true),
                    ToolPreset.create("spin_wheel", "Weekend Fun", listOf("Movie Night", "Gaming", "Walk Outside", "Read a Book", "Cook Meal"), isBuiltIn = true),

                    // Exactly 3 List Picker presets
                    ToolPreset.create("list_picker", "Movie Genres", listOf("Action", "Comedy", "Sci-Fi", "Horror", "Drama", "Animation"), isBuiltIn = true),
                    ToolPreset.create("list_picker", "Board Games", listOf("Catan", "Chess", "Monopoly", "Scrabble", "Ticket to Ride"), isBuiltIn = true),
                    ToolPreset.create("list_picker", "Workout Moves", listOf("Push-ups", "Squats", "Plank 60s", "Jumping Jacks", "Burpees"), isBuiltIn = true),

                    // Exactly 3 Choice presets
                    ToolPreset.create("choice", "Dinner Options", listOf("Cook at Home", "Order Delivery", "Dine Out"), isBuiltIn = true),
                    ToolPreset.create("choice", "Weekend Plan", listOf("Stay In & Relax", "Go Out with Friends", "Outdoor Trip"), isBuiltIn = true),
                    ToolPreset.create("choice", "Movie or Show", listOf("Action Film", "Comedy Series", "Thriller Movie"), isBuiltIn = true)
                )
                presetDao.insertAll(defaults)
            }
        }
    }
}
