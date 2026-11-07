package com.example.register

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Button
import android.widget.CheckBox
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

class RegisterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val txtLogin = findViewById<TextView>(R.id.txtLogin)
        val chkTerms = findViewById<CheckBox>(R.id.chkTerms)

        setupTermsCheckbox(chkTerms)
        setupLoginFooter(txtLogin)

        btnRegister.setOnClickListener {
            if (!chkTerms.isChecked) {
                Toast.makeText(this, R.string.agree_terms_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (password != confirmPassword) {
                Toast.makeText(this, R.string.password_mismatch, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            register(
                name = etName.text.toString(),
                email = etEmail.text.toString(),
                password = password,
            )
        }

        txtLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun setupTermsCheckbox(chkTerms: CheckBox) {
        val prefix = getString(R.string.terms_prefix)
        val service = getString(R.string.terms_service)
        val and = getString(R.string.terms_and)
        val privacy = getString(R.string.terms_privacy)
        val fullText = prefix + service + and + privacy

        val spannable = SpannableString(fullText)
        val linkColor = ContextCompat.getColor(this, R.color.auth_link)

        val serviceStart = prefix.length
        val serviceEnd = serviceStart + service.length
        spannable.setSpan(
            ForegroundColorSpan(linkColor),
            serviceStart,
            serviceEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        spannable.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    Toast.makeText(this@RegisterActivity, service, Toast.LENGTH_SHORT).show()
                }
            },
            serviceStart,
            serviceEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )

        val privacyStart = serviceEnd + and.length
        val privacyEnd = privacyStart + privacy.length
        spannable.setSpan(
            ForegroundColorSpan(linkColor),
            privacyStart,
            privacyEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        spannable.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    Toast.makeText(this@RegisterActivity, privacy, Toast.LENGTH_SHORT).show()
                }
            },
            privacyStart,
            privacyEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )

        chkTerms.text = spannable
        chkTerms.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setupLoginFooter(txtLogin: TextView) {
        val prefix = getString(R.string.already_have_account)
        val login = getString(R.string.log_in)
        val fullText = prefix + login

        val spannable = SpannableString(fullText)
        val loginStart = prefix.length
        val loginEnd = loginStart + login.length

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.auth_link)),
            loginStart,
            loginEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        spannable.setSpan(
            StyleSpan(android.graphics.Typeface.BOLD),
            loginStart,
            loginEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )

        txtLogin.text = spannable
    }

    fun register(name: String, email: String, password: String) {
        val url = "http://10.0.2.2/fitness/register.php"
        val trimmedName = name.trim()
        val trimmedEmail = email.trim().lowercase()

        thread {
            try {
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                }

                val body = buildString {
                    append("name=").append(URLEncoder.encode(trimmedName, "UTF-8"))
                    append("&email=").append(URLEncoder.encode(trimmedEmail, "UTF-8"))
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
                    handleRegisterResponse(response, trimmedEmail)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        e.message ?: "Registration failed",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
        }
    }

    private fun handleRegisterResponse(response: String, email: String) {
        try {
            val json = JSONObject(response)
            val message = json.optString("message", response)
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()

            if (json.optBoolean("success", false)) {
                startActivity(
                    Intent(this, LoginActivity::class.java).apply {
                        putExtra(LoginActivity.EXTRA_EMAIL, email)
                    },
                )
                finish()
            }
        } catch (_: Exception) {
            Toast.makeText(this, response, Toast.LENGTH_LONG).show()
        }
    }
}
