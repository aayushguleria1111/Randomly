package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tool_presets")
data class ToolPreset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val toolId: String,
    val presetName: String,
    val itemsJson: String, // Newline or comma separated items
    val isBuiltIn: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
) {
    fun getItems(): List<String> {
        return itemsJson.lines().map { it.trim() }.filter { it.isNotEmpty() }
    }

    companion object {
        fun create(toolId: String, name: String, items: List<String>, isBuiltIn: Boolean = false): ToolPreset {
            val cleaned = items.map { it.trim() }.filter { it.isNotEmpty() }
            return ToolPreset(
                toolId = toolId,
                presetName = name,
                itemsJson = cleaned.joinToString("\n"),
                isBuiltIn = isBuiltIn,
                createdTimestamp = System.currentTimeMillis()
            )
        }
    }
}
