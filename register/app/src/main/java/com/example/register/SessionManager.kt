package com.example.register

object SessionManager {

    private var loggedIn = false
    private var userName = ""
    private var userEmail = ""
    private var userId = 0

    fun saveSession(name: String, email: String, userId: Int = 0) {
        loggedIn = true
        userName = name
        userEmail = email
        this.userId = userId
    }

    fun updateProfile(name: String) {
        userName = name
    }

    fun isLoggedIn(): Boolean = loggedIn

    fun getUserName(): String = userName

    fun getUserEmail(): String = userEmail

    fun getUserId(): Int = userId

    fun clearSession() {
        loggedIn = false
        userName = ""
        userEmail = ""
        userId = 0
    }
}
