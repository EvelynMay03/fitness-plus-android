package com.example.register.data

import androidx.room.Entity

@Entity(
    tableName = "daily_goals",
    primaryKeys = ["userEmail", "goalDate"],
)
data class DailyGoalEntity(
    val userEmail: String,
    val goalDate: String,
    val targetCalories: Int,
    val createdAt: Long = System.currentTimeMillis(),
)
