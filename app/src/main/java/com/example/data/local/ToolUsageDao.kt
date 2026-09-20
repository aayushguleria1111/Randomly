package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ToolUsage
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolUsageDao {
    @Query("SELECT * FROM tool_usage ORDER BY useCount DESC, lastUsedTimestamp DESC")
    fun getMostUsedTools(): Flow<List<ToolUsage>>

    @Query("SELECT * FROM tool_usage WHERE toolId = :toolId LIMIT 1")
    suspend fun getToolUsage(toolId: String): ToolUsage?

    @Query("INSERT INTO tool_usage (toolId, useCount, lastUsedTimestamp) VALUES (:toolId, 1, :now) ON CONFLICT(toolId) DO UPDATE SET useCount = useCount + 1, lastUsedTimestamp = :now")
    suspend fun incrementUsage(toolId: String, now: Long = System.currentTimeMillis())
}
