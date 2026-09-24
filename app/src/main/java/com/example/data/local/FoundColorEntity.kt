package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "found_colors")
data class FoundColorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val colorId: String,
    val colorName: String,
    val hexTarget: String,
    val hexFound: String,
    val category: String,
    val accuracy: Float,
    val timestamp: Long = System.currentTimeMillis()
)
