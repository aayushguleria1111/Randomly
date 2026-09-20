package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SavedList
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedListDao {
    @Query("SELECT * FROM saved_lists ORDER BY updatedTimestamp DESC")
    fun getAllSavedLists(): Flow<List<SavedList>>

    @Query("SELECT * FROM saved_lists WHERE id = :id")
    suspend fun getSavedListById(id: Long): SavedList?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedList(list: SavedList): Long

    @Update
    suspend fun updateSavedList(list: SavedList)

    @Query("DELETE FROM saved_lists WHERE id = :id")
    suspend fun deleteSavedListById(id: Long)
}
