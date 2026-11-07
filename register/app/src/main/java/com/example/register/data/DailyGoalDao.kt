package com.example.register.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DailyGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: DailyGoalEntity)

    @Query(
        """
        SELECT * FROM daily_goals
        WHERE userEmail = :email AND goalDate = :date
        LIMIT 1
        """,
    )
    suspend fun getGoal(email: String, date: String): DailyGoalEntity?
}
