package com.example.register

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.register.data.WorkoutEntity
import com.example.register.data.WorkoutRepository
import com.example.register.model.ActivityType
import kotlinx.coroutines.launch

class LogWorkoutActivity : ComponentActivity() {

    private lateinit var activityType: ActivityType
    private lateinit var repository: WorkoutRepository
    private lateinit var etDuration: EditText
    private lateinit var etWeight: EditText
    private lateinit var txtEstimatedCalories: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val typeKey = intent.getStringExtra(EXTRA_ACTIVITY_TYPE)
        activityType = ActivityType.fromKey(typeKey) ?: ActivityType.RUNNING

        setContentView(R.layout.activity_log_workout)
        repository = WorkoutRepository(this)

        etDuration = findViewById(R.id.etDuration)
        etWeight = findViewById(R.id.etWeight)
        txtEstimatedCalories = findViewById(R.id.txtEstimatedCalories)

        findViewById<TextView>(R.id.txtLogTitle).setText(activityType.labelRes)
        findViewById<TextView>(R.id.txtLogEmoji).text = activityType.emoji
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        val updateCalories = {
            updateEstimatedCalories()
        }
        etDuration.doAfterTextChanged { updateCalories() }
        etWeight.doAfterTextChanged { updateCalories() }

        findViewById<Button>(R.id.btnSaveWorkout).setOnClickListener {
            saveWorkout()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!SessionManager.isLoggedIn()) {
            finish()
        }
    }

    private fun updateEstimatedCalories() {
        val duration = etDuration.text.toString().trim().toIntOrNull() ?: 0
        val weight = etWeight.text.toString().trim().toDoubleOrNull() ?: 0.0
        val calories = CalorieCalculator.calculate(activityType.met, weight, duration)

        txtEstimatedCalories.text = if (calories > 0) {
            getString(R.string.log_detail_calories, calories)
        } else {
            getString(R.string.log_calories_empty)
        }
    }

    private fun saveWorkout() {
        val durationText = etDuration.text.toString().trim()
        if (durationText.isEmpty()) {
            Toast.makeText(this, R.string.log_error_duration, Toast.LENGTH_SHORT).show()
            return
        }

        val duration = durationText.toIntOrNull()
        if (duration == null || duration <= 0) {
            Toast.makeText(this, R.string.log_error_duration, Toast.LENGTH_SHORT).show()
            return
        }

        val weightText = etWeight.text.toString().trim()
        if (weightText.isEmpty()) {
            Toast.makeText(this, R.string.log_error_weight, Toast.LENGTH_SHORT).show()
            return
        }

        val weight = weightText.toDoubleOrNull()
        if (weight == null || weight <= 0) {
            Toast.makeText(this, R.string.log_error_weight, Toast.LENGTH_SHORT).show()
            return
        }

        val calories = CalorieCalculator.calculate(activityType.met, weight, duration)

        val workout = WorkoutEntity(
            userEmail = SessionManager.getUserEmail(),
            activityType = activityType.key,
            durationMinutes = duration,
            weightKg = weight,
            calories = calories,
        )

        lifecycleScope.launch {
            val result = repository.logWorkout(workout)
            runOnUiThread {
                if (result.isSuccess) {
                    Toast.makeText(
                        this@LogWorkoutActivity,
                        getString(R.string.log_saved_with_calories, calories),
                        Toast.LENGTH_SHORT,
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@LogWorkoutActivity,
                        result.exceptionOrNull()?.message ?: getString(R.string.log_error_save),
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
        }
    }

    companion object {
        const val EXTRA_ACTIVITY_TYPE = "extra_activity_type"
    }
}
