package com.example.register

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.example.register.data.DailyGoalEntity
import com.example.register.data.DailyGoalRepository
import com.example.register.model.ActivityType
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var goalRepository: DailyGoalRepository
    private var currentGoal: DailyGoalEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        goalRepository = DailyGoalRepository(this)
        setupMainScreen()
    }

    override fun onResume() {
        super.onResume()
        if (!SessionManager.isLoggedIn()) {
            goToLogin()
            return
        }
        loadTodayGoal()
    }

    private fun setupMainScreen() {
        val userName = SessionManager.getUserName().ifBlank { "User" }
        findViewById<TextView>(R.id.txtHello).text = getString(R.string.home_hello, userName)

        findViewById<ImageView>(R.id.btnNotification).setOnClickListener {
            Toast.makeText(this, R.string.home_no_notifications, Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.txtSeeAll).setOnClickListener {
            NavigationHelper.openActivities(this)
        }

        findViewById<Button>(R.id.btnSetGoal).setOnClickListener {
            showGoalDialog(currentGoal)
        }

        findViewById<TextView>(R.id.btnEditGoal).setOnClickListener {
            showGoalDialog(currentGoal)
        }

        setupCategoryChips()

        findViewById<FrameLayout>(R.id.cardFunctional).setOnClickListener {
            Toast.makeText(this, R.string.youtube_opening, Toast.LENGTH_SHORT).show()
            YouTubeHelper.openVideo(this, getString(R.string.youtube_video_functional))
        }

        findViewById<FrameLayout>(R.id.cardYoga).setOnClickListener {
            Toast.makeText(this, R.string.youtube_opening, Toast.LENGTH_SHORT).show()
            YouTubeHelper.openVideo(this, getString(R.string.youtube_video_yoga))
        }

        findViewById<FrameLayout>(R.id.cardCardio).setOnClickListener {
            Toast.makeText(this, R.string.youtube_opening, Toast.LENGTH_SHORT).show()
            YouTubeHelper.openVideo(this, getString(R.string.youtube_video_cardio))
        }

        findViewById<ImageView>(R.id.navStats).setOnClickListener {
            NavigationHelper.openActivities(this)
        }

        findViewById<ImageView>(R.id.navProfile).setOnClickListener {
            NavigationHelper.openProfile(this)
        }

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabOpenMap)
            .setOnClickListener {
                startActivity(Intent(this, MapActivity::class.java))
            }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    moveTaskToBack(true)
                }
            },
        )
    }

    private fun loadTodayGoal() {
        val email = SessionManager.getUserEmail()
        lifecycleScope.launch {
            val status = goalRepository.getTodayGoalStatus(email)
            runOnUiThread {
                renderTodayGoal(
                    goal = status.goal,
                    burnedCalories = status.burnedCalories,
                    progressPercent = status.progressPercent,
                    isComplete = status.isComplete,
                )
            }
        }
    }

    private fun renderTodayGoal(
        goal: DailyGoalEntity?,
        burnedCalories: Int,
        progressPercent: Int,
        isComplete: Boolean,
    ) {
        currentGoal = goal

        val emptyState = findViewById<LinearLayout>(R.id.goalEmptyState)
        val activeState = findViewById<LinearLayout>(R.id.goalActiveState)
        val btnEditGoal = findViewById<TextView>(R.id.btnEditGoal)
        val txtBurned = findViewById<TextView>(R.id.txtGoalBurned)
        val txtTarget = findViewById<TextView>(R.id.txtGoalTarget)
        val txtProgress = findViewById<TextView>(R.id.txtGoalProgress)
        val progressBar = findViewById<ProgressBar>(R.id.progressGoal)

        if (goal == null) {
            emptyState.visibility = View.VISIBLE
            activeState.visibility = View.GONE
            btnEditGoal.visibility = View.GONE
            return
        }

        emptyState.visibility = View.GONE
        activeState.visibility = View.VISIBLE
        btnEditGoal.visibility = View.VISIBLE

        txtBurned.text = burnedCalories.toString()
        txtTarget.text = goal.targetCalories.toString()
        progressBar.progress = progressPercent
        txtProgress.text = when {
            isComplete -> getString(R.string.home_goal_complete)
            else -> getString(
                R.string.home_goal_progress,
                burnedCalories,
                goal.targetCalories,
            ) + " · " + getString(R.string.home_goal_percent, progressPercent)
        }
    }

    private fun showGoalDialog(existing: DailyGoalEntity?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_daily_goal)

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                (resources.displayMetrics.widthPixels * 0.88f).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }

        val txtDialogTitle = dialog.findViewById<TextView>(R.id.txtDialogTitle)
        val etGoalCalories = dialog.findViewById<EditText>(R.id.etGoalCalories)

        txtDialogTitle.setText(
            if (existing == null) {
                R.string.home_goal_dialog_title
            } else {
                R.string.home_goal_dialog_edit_title
            },
        )

        existing?.let { goal ->
            etGoalCalories.setText(goal.targetCalories.toString())
        }

        dialog.findViewById<Button>(R.id.btnDialogCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnDialogSave).setOnClickListener {
            saveTodayGoal(etGoalCalories, dialog)
        }

        dialog.show()
    }

    private fun saveTodayGoal(etGoalCalories: EditText, dialog: Dialog) {
        val calories = etGoalCalories.text.toString().trim().toIntOrNull()

        if (calories == null || calories <= 0) {
            Toast.makeText(this, R.string.home_goal_error_calories, Toast.LENGTH_SHORT).show()
            return
        }

        val email = SessionManager.getUserEmail()
        lifecycleScope.launch {
            val result = goalRepository.saveTodayGoal(
                email = email,
                userId = SessionManager.getUserId(),
                targetCalories = calories,
            )
            runOnUiThread {
                if (result.isSuccess) {
                    dialog.dismiss()
                    Toast.makeText(this@MainActivity, R.string.home_goal_saved, Toast.LENGTH_SHORT).show()
                    loadTodayGoal()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        result.exceptionOrNull()?.message ?: getString(R.string.log_error_save),
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
        }
    }

    private fun setupCategoryChips() {
        val container = findViewById<LinearLayout>(R.id.categoriesContainer)
        val inflater = LayoutInflater.from(this)
        val iconBackgrounds = listOf(
            R.drawable.home_category_icon_creme,
            R.drawable.home_category_icon_mustard,
            R.drawable.home_category_icon_navy_soft,
        )

        ActivityType.entries.forEachIndexed { index, type ->
            val chip = inflater.inflate(R.layout.item_home_category, container, false)
            val iconBgRes = iconBackgrounds[index % iconBackgrounds.size]

            chip.findViewById<TextView>(R.id.txtCategoryEmoji).apply {
                text = type.emoji
                setBackgroundResource(iconBgRes)
            }
            chip.findViewById<TextView>(R.id.txtCategoryLabel).setText(type.labelRes)
            chip.setOnClickListener {
                NavigationHelper.openLogWorkout(this, type)
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                marginEnd = if (index < ActivityType.entries.size - 1) dp(10) else 0
            }
            container.addView(chip, params)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
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
