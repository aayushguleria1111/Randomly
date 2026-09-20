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
    version = 2,
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
                        // Ensure built-in presets exist if DB was populated
                        val count = database.toolPresetDao().getPresetCount("spin_wheel")
                        if (count == 0) {
                            seedDefaultPresets(database.toolPresetDao())
                        }
                    }
                }
            }

            private suspend fun seedDefaultPresets(presetDao: ToolPresetDao) {
                val defaults = listOf(
                    ToolPreset.create("spin_wheel", "Food & Meals", listOf("Pizza", "Burger", "Sushi", "Tacos", "Pasta", "Salad", "Ramen"), isBuiltIn = true),
                    ToolPreset.create("spin_wheel", "Yes / No / Maybe", listOf("Yes", "No", "Maybe", "Ask Again", "Definitely"), isBuiltIn = true),
                    ToolPreset.create("spin_wheel", "Weekend Activities", listOf("Watch Movies", "Play Games", "Read a Book", "Walk Outside", "Cook Dinner", "Gym Workout"), isBuiltIn = true),
                    ToolPreset.create("spin_wheel", "Truth or Dare", listOf("Truth", "Dare", "Double Dare", "Pass", "Wildcard"), isBuiltIn = true),
                    ToolPreset.create("list_picker", "Movie Genres", listOf("Action", "Comedy", "Sci-Fi", "Horror", "Drama", "Animation", "Documentary"), isBuiltIn = true),
                    ToolPreset.create("list_picker", "Board Games", listOf("Catan", "Chess", "Monopoly", "Scrabble", "Ticket to Ride", "Codenames"), isBuiltIn = true),
                    ToolPreset.create("list_picker", "Workout Moves", listOf("Push-ups", "Squats", "Burpees", "Plank 60s", "Jumping Jacks", "Lunges"), isBuiltIn = true),
                    ToolPreset.create("choice", "Dinner Options", listOf("Cook at Home", "Order Takeout", "Go Out to Restaurant"), isBuiltIn = true)
                )
                presetDao.insertAll(defaults)
            }
        }
    }
}
