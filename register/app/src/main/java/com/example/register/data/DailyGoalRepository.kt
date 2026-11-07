package com.example.register.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DailyGoalRepository(context: Context) {

    private val dailyGoalDao = AppDatabase.getInstance(context).dailyGoalDao()
    private val workoutDao = AppDatabase.getInstance(context).workoutDao()

    data class TodayGoalStatus(
        val goal: DailyGoalEntity?,
        val burnedCalories: Int,
    ) {
        val progressPercent: Int
            get() {
                val target = goal?.targetCalories ?: return 0
                if (target <= 0) return 0
                return ((burnedCalories * 100) / target).coerceIn(0, 100)
            }

        val isComplete: Boolean
            get() = goal != null && burnedCalories >= goal.targetCalories
    }

    suspend fun getTodayGoalStatus(email: String): TodayGoalStatus = withContext(Dispatchers.IO) {
        val today = todayDateString()
        val goal = dailyGoalDao.getGoal(email, today)
        val burnedCalories = workoutDao.totalCaloriesSince(email, startOfTodayMillis())
        TodayGoalStatus(goal = goal, burnedCalories = burnedCalories)
    }

    suspend fun saveTodayGoal(
        email: String,
        userId: Int,
        targetCalories: Int,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val goalDate = todayDateString()
            dailyGoalDao.upsert(
                DailyGoalEntity(
                    userEmail = email,
                    goalDate = goalDate,
                    targetCalories = targetCalories,
                ),
            )
            syncToServer(
                userId = userId,
                email = email,
                goals = targetCalories,
                goalDate = goalDate,
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun syncToServer(userId: Int, email: String, goals: Int, goalDate: String) {
        try {
            val connection = (URL("http://10.0.2.2/fitness/goals.php").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            }

            val body = buildString {
                if (userId > 0) {
                    append("user_id=").append(userId)
                } else {
                    append("email=").append(URLEncoder.encode(email, "UTF-8"))
                }
                append("&goals=").append(goals)
                append("&goal_date=").append(URLEncoder.encode(goalDate, "UTF-8"))
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

    private fun todayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun startOfTodayMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
