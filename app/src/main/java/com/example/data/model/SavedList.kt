package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray

@Entity(tableName = "saved_lists")
data class SavedList(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val itemsJson: String,
    val updatedTimestamp: Long = System.currentTimeMillis()
) {
    fun getItems(): List<String> {
        return try {
            val jsonArray = JSONArray(itemsJson)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        fun fromList(name: String, items: List<String>, id: Long = 0): SavedList {
            val jsonArray = JSONArray()
            items.forEach { jsonArray.put(it) }
            return SavedList(
                id = id,
                name = name,
                itemsJson = jsonArray.toString(),
                updatedTimestamp = System.currentTimeMillis()
            )
        }
    }
}
