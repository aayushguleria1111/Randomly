package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ToolPreset
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolPresetDao {
    @Query("SELECT * FROM tool_presets WHERE toolId = :toolId ORDER BY isBuiltIn DESC, createdTimestamp DESC")
    fun getPresetsForTool(toolId: String): Flow<List<ToolPreset>>

    @Query("SELECT * FROM tool_presets ORDER BY createdTimestamp DESC")
    fun getAllPresets(): Flow<List<ToolPreset>>

    @Query("SELECT COUNT(*) FROM tool_presets WHERE toolId = :toolId")
    suspend fun getPresetCount(toolId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: ToolPreset): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(presets: List<ToolPreset>)

    @Query("DELETE FROM tool_presets WHERE id = :id")
    suspend fun deletePresetById(id: Long)

    @Query("DELETE FROM tool_presets WHERE isBuiltIn = 1")
    suspend fun deleteBuiltInPresets()

    @Query("SELECT COUNT(*) FROM tool_presets WHERE isBuiltIn = 1 AND toolId = :toolId")
    suspend fun getBuiltInPresetCountForTool(toolId: String): Int
}
