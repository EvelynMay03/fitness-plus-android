package com.example.register.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ActivityRepository(context: Context) {

    private val activityDao = AppDatabase.getInstance(context).activityDao()

    suspend fun getActivities(): List<ActivityEntity> = withContext(Dispatchers.IO) {
        ensureSeeded()
        syncFromServer()
        activityDao.getAll()
    }

    private suspend fun ensureSeeded() {
        if (activityDao.count() == 0) {
            activityDao.insertAll(defaultActivities())
        }
    }

    private suspend fun syncFromServer() {
        try {
            val connection = (URL("http://10.0.2.2/fitness/activities.php").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
            }

            val response = connection.inputStream.use { stream ->
                BufferedReader(InputStreamReader(stream)).readText()
            }
            connection.disconnect()

            val activities = parseActivities(response)
            if (activities.isNotEmpty()) {
                activityDao.insertAll(activities)
            }
        } catch (_: Exception) {
            // Keep local seeded activities when server is unavailable.
        }
    }

    private fun parseActivities(json: String): List<ActivityEntity> {
        val array = JSONArray(json)
        val activities = mutableListOf<ActivityEntity>()

        for (index in 0 until array.length()) {
            val item = array.getJSONObject(index)
            activities.add(
                ActivityEntity(
                    activityKey = item.getString("activity_key"),
                    name = item.getString("name"),
                    description = item.optString("description").ifBlank { null },
                    emoji = item.optString("emoji").ifBlank { null },
                ),
            )
        }

        return activities
    }

    private fun defaultActivities(): List<ActivityEntity> = listOf(
        ActivityEntity(activityKey = "running", name = "Running", description = "Duration and weight", emoji = "🏃"),
        ActivityEntity(activityKey = "cycling", name = "Cycling", description = "Duration and weight", emoji = "🚴"),
        ActivityEntity(activityKey = "weightlifting", name = "Weightlifting", description = "Duration and weight lifted", emoji = "🏋️"),
        ActivityEntity(activityKey = "yoga", name = "Yoga", description = "Duration and weight", emoji = "🧘"),
        ActivityEntity(activityKey = "cardio", name = "Cardio", description = "Duration and weight", emoji = "🤸"),
        ActivityEntity(activityKey = "swimming", name = "Swimming", description = "Duration and weight", emoji = "🏊"),
        ActivityEntity(activityKey = "hiking", name = "Hiking", description = "Duration and weight", emoji = "🥾"),
        ActivityEntity(activityKey = "walking", name = "Walking", description = "Duration and weight", emoji = "🚶"),
    )
}
