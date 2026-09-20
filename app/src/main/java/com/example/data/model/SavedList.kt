package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_lists")
data class SavedList(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val itemsJson: String, // Comma-separated or newline-delimited items
    val itemCount: Int = 0,
    val updatedTimestamp: Long = System.currentTimeMillis()
) {
    fun getItems(): List<String> {
        return itemsJson.lines().map { it.trim() }.filter { it.isNotEmpty() }
    }

    companion object {
        fun fromList(name: String, items: List<String>, id: Long = 0): SavedList {
            val filtered = items.map { it.trim() }.filter { it.isNotEmpty() }
            return SavedList(
                id = id,
                name = name,
                itemsJson = filtered.joinToString("\n"),
                itemCount = filtered.size,
                updatedTimestamp = System.currentTimeMillis()
            )
        }
    }
}
