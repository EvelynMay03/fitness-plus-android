package com.example.register

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import android.app.Dialog

class ProfileActivity : ComponentActivity() {

    private val profileRepository = ProfileRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile)
        loadProfile()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        if (!SessionManager.isLoggedIn()) {
            goToLogin()
        }
    }

    private fun loadProfile() {
        val name = SessionManager.getUserName().ifBlank { "User" }
        val email = SessionManager.getUserEmail()

        findViewById<TextView>(R.id.txtAvatarInitial).text =
            name.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
        findViewById<TextView>(R.id.txtProfileName).text = name
        findViewById<TextView>(R.id.txtProfileEmail).text = email
        findViewById<EditText>(R.id.etProfileName).setText(name)
        findViewById<EditText>(R.id.etProfileEmail).setText(email)
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnSaveProfile).setOnClickListener {
            saveProfile()
        }

        findViewById<Button>(R.id.btnCalculateBmi).setOnClickListener {
            calculateBmi()
        }

        findViewById<Button>(R.id.btnChangePassword).setOnClickListener {
            changePassword()
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            confirmLogout()
        }

        findViewById<ImageView>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<ImageView>(R.id.navStats).setOnClickListener {
            startActivity(Intent(this, ActivitiesActivity::class.java))
            finish()
        }
    }

    private fun saveProfile() {
        val name = findViewById<EditText>(R.id.etProfileName).text.toString().trim()
        val email = SessionManager.getUserEmail()

        if (name.isEmpty()) {
            Toast.makeText(this, R.string.profile_error_name, Toast.LENGTH_SHORT).show()
            return
        }

        profileRepository.updateProfile(email, name) { result ->
            runOnUiThread {
                when (result) {
                    is ProfileRepository.Result.Success -> {
                        SessionManager.updateProfile(name)
                        loadProfile()
                        Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show()
                    }
                    is ProfileRepository.Result.Error -> {
                        SessionManager.updateProfile(name)
                        loadProfile()
                        Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun calculateBmi() {
        val height = findViewById<EditText>(R.id.etHeight).text.toString().trim().toDoubleOrNull()
        val weight = findViewById<EditText>(R.id.etWeight).text.toString().trim().toDoubleOrNull()

        val result = if (height != null && weight != null) {
            BmiCalculator.calculate(weight, height)
        } else {
            null
        }

        val resultCard = findViewById<LinearLayout>(R.id.bmiResultCard)
        if (result == null) {
            resultCard.visibility = View.GONE
            Toast.makeText(this, R.string.profile_bmi_error, Toast.LENGTH_SHORT).show()
            return
        }

        findViewById<TextView>(R.id.txtBmiValue).text =
            String.format(java.util.Locale.US, "%.1f", result.bmi)
        findViewById<TextView>(R.id.txtBmiCategory).text =
            getString(result.category.labelRes)
        resultCard.visibility = View.VISIBLE
    }

    private fun changePassword() {
        val current = findViewById<EditText>(R.id.etCurrentPassword).text.toString()
        val newPass = findViewById<EditText>(R.id.etNewPassword).text.toString()
        val confirm = findViewById<EditText>(R.id.etConfirmNewPassword).text.toString()

        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, R.string.profile_error_password_fields, Toast.LENGTH_SHORT).show()
            return
        }

        if (newPass.length < 6) {
            Toast.makeText(this, R.string.profile_error_password_length, Toast.LENGTH_SHORT).show()
            return
        }

        if (newPass != confirm) {
            Toast.makeText(this, R.string.password_mismatch, Toast.LENGTH_SHORT).show()
            return
        }

        val email = SessionManager.getUserEmail()
        profileRepository.changePassword(email, current, newPass) { result ->
            runOnUiThread {
                when (result) {
                    is ProfileRepository.Result.Success -> {
                        findViewById<EditText>(R.id.etCurrentPassword).text.clear()
                        findViewById<EditText>(R.id.etNewPassword).text.clear()
                        findViewById<EditText>(R.id.etConfirmNewPassword).text.clear()
                        Toast.makeText(this, R.string.profile_password_updated, Toast.LENGTH_SHORT).show()
                    }
                    is ProfileRepository.Result.Error -> {
                        Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun confirmLogout() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logout_confirm)

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                (resources.displayMetrics.widthPixels * 0.88f).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }

        dialog.findViewById<Button>(R.id.btnLogoutCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnLogoutConfirm).setOnClickListener {
            dialog.dismiss()
            SessionManager.clearSession()
            goToLogin()
        }

        dialog.show()
    }

    private fun goToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
        )
        finish()
    }
}
