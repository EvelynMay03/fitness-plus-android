package com.example.register.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WorkoutDao {

    @Insert
    suspend fun insert(workout: WorkoutEntity): Long

    @Query(
        """
        SELECT * FROM workouts
        WHERE userEmail = :email
        ORDER BY loggedAt DESC
        LIMIT :limit
        """,
    )
    suspend fun getRecentByUser(email: String, limit: Int = 20): List<WorkoutEntity>

    @Query(
        """
        SELECT COUNT(*) FROM workouts
        WHERE userEmail = :email
        AND loggedAt >= :since
        """,
    )
    suspend fun countSince(email: String, since: Long): Int

    @Query(
        """
        SELECT COALESCE(SUM(durationMinutes), 0) FROM workouts
        WHERE userEmail = :email
        AND loggedAt >= :since
        """,
    )
    suspend fun totalMinutesSince(email: String, since: Long): Int

    @Query(
        """
        SELECT COALESCE(SUM(calories), 0) FROM workouts
        WHERE userEmail = :email
        AND loggedAt >= :since
        """,
    )
    suspend fun totalCaloriesSince(email: String, since: Long): Int
}
