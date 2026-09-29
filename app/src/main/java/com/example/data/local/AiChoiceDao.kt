package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AiChoiceItem
import kotlinx.coroutines.flow.Flow

@Dao
interface AiChoiceDao {
    @Query("SELECT * FROM ai_choices ORDER BY timestamp DESC")
    fun getAllChoices(): Flow<List<AiChoiceItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChoice(item: AiChoiceItem): Long

    @Query("DELETE FROM ai_choices WHERE id = :id")
    suspend fun deleteChoiceById(id: Long)

    @Query("DELETE FROM ai_choices")
    suspend fun clearAllChoices()
}
