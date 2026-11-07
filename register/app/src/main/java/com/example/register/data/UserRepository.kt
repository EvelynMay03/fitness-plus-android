package com.example.register.data

import android.util.Patterns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class UserRepository(private val userDao: UserDao) {

    sealed class RegisterResult {
        data object Success : RegisterResult()
        data class Error(val message: String) : RegisterResult()
    }

    sealed class LoginResult {
        data class Success(val name: String) : LoginResult()
        data class Error(val message: String) : LoginResult()
    }

    suspend fun register(name: String, email: String, password: String): RegisterResult =
        withContext(Dispatchers.IO) {
            val trimmedName = name.trim()
            val trimmedEmail = email.trim().lowercase()

            if (trimmedName.isEmpty() || trimmedEmail.isEmpty() || password.isEmpty()) {
                return@withContext RegisterResult.Error("Please fill in all fields")
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                return@withContext RegisterResult.Error("Please enter a valid email")
            }
            if (password.length < 6) {
                return@withContext RegisterResult.Error("Password must be at least 6 characters")
            }

            try {
                if (userDao.findByEmail(trimmedEmail) != null) {
                    return@withContext RegisterResult.Error("An account with this email already exists")
                }

                val rowId = userDao.insertUser(
                    UserEntity(
                        name = trimmedName,
                        email = trimmedEmail,
                        passwordHash = hashPassword(password),
                    ),
                )
                if (rowId <= 0L) {
                    return@withContext RegisterResult.Error("Failed to save user")
                }

                val saved = userDao.findByEmail(trimmedEmail)
                if (saved == null) {
                    return@withContext RegisterResult.Error("User was not saved to the database")
                }

                RegisterResult.Success
            } catch (e: Exception) {
                RegisterResult.Error(e.message ?: "Registration failed. Please try again")
            }
        }

    suspend fun login(email: String, password: String): LoginResult =
        withContext(Dispatchers.IO) {
            val trimmedEmail = email.trim().lowercase()

            if (trimmedEmail.isEmpty() || password.isEmpty()) {
                return@withContext LoginResult.Error("Please enter email and password")
            }

            try {
                val user = userDao.findByEmail(trimmedEmail)
                    ?: return@withContext LoginResult.Error("Invalid email or password")

                if (user.passwordHash == hashPassword(password)) {
                    LoginResult.Success(user.name)
                } else {
                    LoginResult.Error("Invalid email or password")
                }
            } catch (e: Exception) {
                LoginResult.Error(e.message ?: "Login failed. Please try again")
            }
        }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
    }
}
