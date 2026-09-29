package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_choices")
data class AiChoiceItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val choice: String,
    val reasoning: String,
    val preferenceMode: String = "Balanced",
    val customContext: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
