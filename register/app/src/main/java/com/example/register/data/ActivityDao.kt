package com.example.register.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ActivityDao {

    @Query("SELECT * FROM activities ORDER BY id ASC")
    suspend fun getAll(): List<ActivityEntity>

    @Query("SELECT COUNT(*) FROM activities")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activities: List<ActivityEntity>)
}
