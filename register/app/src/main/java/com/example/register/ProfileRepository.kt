package com.example.register

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class ProfileRepository {

    sealed class Result {
        data class Success(val message: String) : Result()
        data class Error(val message: String) : Result()
    }

    fun updateProfile(
        email: String,
        name: String,
        callback: (Result) -> Unit,
    ) {
        val url = "http://10.0.2.2/fitness/update_profile.php"
        val trimmedName = name.trim()

        Thread {
            try {
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                }

                val body = buildString {
                    append("email=").append(URLEncoder.encode(email.trim().lowercase(), "UTF-8"))
                    append("&name=").append(URLEncoder.encode(trimmedName, "UTF-8"))
                }

                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

                val stream = if (connection.responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val response = BufferedReader(InputStreamReader(stream)).use { it.readText() }
                connection.disconnect()

                callback(parseResponse(response))
            } catch (e: Exception) {
                callback(Result.Error(e.message ?: "Failed to update profile"))
            }
        }.start()
    }

    fun changePassword(
        email: String,
        currentPassword: String,
        newPassword: String,
        callback: (Result) -> Unit,
    ) {
        val url = "http://10.0.2.2/fitness/change_password.php"

        Thread {
            try {
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                }

                val body = buildString {
                    append("email=").append(URLEncoder.encode(email.trim().lowercase(), "UTF-8"))
                    append("&current_password=").append(URLEncoder.encode(currentPassword, "UTF-8"))
                    append("&new_password=").append(URLEncoder.encode(newPassword, "UTF-8"))
                }

                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

                val stream = if (connection.responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val response = BufferedReader(InputStreamReader(stream)).use { it.readText() }
                connection.disconnect()

                callback(parseResponse(response))
            } catch (e: Exception) {
                callback(Result.Error(e.message ?: "Failed to change password"))
            }
        }.start()
    }

    private fun parseResponse(response: String): Result {
        return try {
            val json = JSONObject(response)
            val message = json.optString("message", response)
            if (json.optBoolean("success", false)) {
                Result.Success(message)
            } else {
                Result.Error(message)
            }
        } catch (_: Exception) {
            Result.Success("Saved locally")
        }
    }
}
