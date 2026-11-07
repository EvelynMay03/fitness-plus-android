package com.example.register.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Calendar

class WorkoutRepository(context: Context) {

    private val workoutDao = AppDatabase.getInstance(context).workoutDao()

    data class WeeklyStats(
        val workoutCount: Int,
        val totalMinutes: Int,
        val totalCalories: Int,
    )

    suspend fun logWorkout(workout: WorkoutEntity): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val id = workoutDao.insert(workout)
            syncToServer(workout)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentWorkouts(email: String): List<WorkoutEntity> =
        withContext(Dispatchers.IO) {
            workoutDao.getRecentByUser(email)
        }

    suspend fun getWeeklyStats(email: String): WeeklyStats = withContext(Dispatchers.IO) {
        val weekStart = startOfWeekMillis()
        WeeklyStats(
            workoutCount = workoutDao.countSince(email, weekStart),
            totalMinutes = workoutDao.totalMinutesSince(email, weekStart),
            totalCalories = workoutDao.totalCaloriesSince(email, weekStart),
        )
    }

    private fun startOfWeekMillis(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun syncToServer(workout: WorkoutEntity) {
        try {
            val connection = (URL("http://10.0.2.2/fitness/workouts.php").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            }

            val body = buildString {
                append("email=").append(URLEncoder.encode(workout.userEmail, "UTF-8"))
                append("&activity_type=").append(URLEncoder.encode(workout.activityType, "UTF-8"))
                append("&duration_minutes=").append(workout.durationMinutes)
                workout.distanceKm?.let { append("&distance_km=").append(it) }
                workout.calories?.let { append("&calories=").append(it) }
                workout.sets?.let { append("&sets=").append(it) }
                workout.reps?.let { append("&reps=").append(it) }
                workout.weightKg?.let { append("&weight_kg=").append(it) }
                workout.notes?.let { append("&notes=").append(URLEncoder.encode(it, "UTF-8")) }
            }

            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            connection.inputStream.use { stream ->
                BufferedReader(InputStreamReader(stream)).readText()
            }
            connection.disconnect()
        } catch (_: Exception) {
            // Local save succeeds even if server sync fails offline.
        }
    }
}
