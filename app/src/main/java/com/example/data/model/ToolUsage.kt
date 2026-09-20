package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tool_usage")
data class ToolUsage(
    @PrimaryKey
    val toolId: String,
    val useCount: Int = 1,
    val lastUsedTimestamp: Long = System.currentTimeMillis()
)
