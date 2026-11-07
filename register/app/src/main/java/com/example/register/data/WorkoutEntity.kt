package com.example.register.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val activityType: String,
    val durationMinutes: Int,
    val distanceKm: Double? = null,
    val calories: Int? = null,
    val sets: Int? = null,
    val reps: Int? = null,
    val weightKg: Double? = null,
    val notes: String? = null,
    val loggedAt: Long = System.currentTimeMillis(),
)
