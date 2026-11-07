package com.example.register.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activities",
    indices = [Index(value = ["activityKey"], unique = true)],
)
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val activityKey: String,
    val name: String,
    val description: String? = null,
    val emoji: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
