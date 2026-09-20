package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val toolType: String,
    val title: String,
    val result: String,
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteItem(
    @PrimaryKey
    val toolId: String,
    val addedTimestamp: Long = System.currentTimeMillis()
)
