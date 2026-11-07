package com.example.register

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.concurrent.thread

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.login)

        val loginEmail = findViewById<EditText>(R.id.loginEmail)
        val loginPassword = findViewById<EditText>(R.id.loginPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtRegister = findViewById<TextView>(R.id.txtRegister)

        setupRegisterFooter(txtRegister)

        intent.getStringExtra(EXTRA_EMAIL)?.let { email ->
            loginEmail.setText(email)
        }

        btnLogin.setOnClickListener {
            login(
                email = loginEmail.text.toString(),
                password = loginPassword.text.toString(),
            )
        }

        txtRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun setupRegisterFooter(txtRegister: TextView) {
        val prefix = getString(R.string.dont_have_account)
        val signUp = getString(R.string.sign_up_link)
        val fullText = prefix + signUp

        val spannable = SpannableString(fullText)
        val signUpStart = prefix.length
        val signUpEnd = signUpStart + signUp.length

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.auth_link)),
            signUpStart,
            signUpEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        spannable.setSpan(
            StyleSpan(android.graphics.Typeface.BOLD),
            signUpStart,
            signUpEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )

        txtRegister.text = spannable
    }

    fun login(email: String, password: String) {
        val url = "http://10.0.2.2/fitness/login.php"
        val trimmedEmail = email.trim().lowercase()

        thread {
            try {
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                }

                val body = buildString {
                    append("email=").append(URLEncoder.encode(trimmedEmail, "UTF-8"))
                    append("&password=").append(URLEncoder.encode(password, "UTF-8"))
                }

                connection.outputStream.use { stream ->
                    stream.write(body.toByteArray(Charsets.UTF_8))
                }

                val responseCode = connection.responseCode
                val stream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val response = BufferedReader(InputStreamReader(stream)).use { reader ->
                    reader.readText()
                }
                connection.disconnect()

                runOnUiThread {
                    handleLoginResponse(response, trimmedEmail)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        e.message ?: "Login failed",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
        }
    }

    private fun handleLoginResponse(response: String, email: String) {
        try {
            val json = JSONObject(response)
            val message = json.optString("message", response)

            if (json.optBoolean("success", false)) {
                val name = json.optString("name", "User")
                val userId = when {
                    json.has("user_id") -> json.optInt("user_id")
                    json.has("userid") -> json.optInt("userid")
                    else -> json.optInt("id")
                }
                SessionManager.saveSession(name, email, userId)
                Toast.makeText(
                    this,
                    "Welcome back, $name!",
                    Toast.LENGTH_SHORT,
                ).show()
                goToHome()
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        } catch (_: Exception) {
            Toast.makeText(this, response, Toast.LENGTH_LONG).show()
        }
    }

    private fun goToHome() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
        )
        finish()
    }

    companion object {
        const val EXTRA_EMAIL = "extra_email"
    }
}
