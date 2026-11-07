package com.example.register

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.register.data.ActivityRepository
import com.example.register.data.DailyGoalEntity
import com.example.register.data.DailyGoalRepository
import com.example.register.data.WorkoutEntity
import com.example.register.data.WorkoutRepository
import com.example.register.model.ActivityType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivitiesActivity : ComponentActivity() {

    private lateinit var repository: WorkoutRepository
    private lateinit var activityRepository: ActivityRepository
    private lateinit var goalRepository: DailyGoalRepository
    private lateinit var activityTypeAdapter: ActivityTypeAdapter
    private lateinit var rvActivityTypes: RecyclerView
    private lateinit var recentLogsContainer: LinearLayout
    private lateinit var txtEmptyLogs: TextView
    private lateinit var txtStatWorkouts: TextView
    private lateinit var txtStatMinutes: TextView
    private lateinit var txtStatCalories: TextView
    private var currentGoal: DailyGoalEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activities_main)
        repository = WorkoutRepository(this)
        activityRepository = ActivityRepository(this)
        goalRepository = DailyGoalRepository(this)

        recentLogsContainer = findViewById(R.id.recentLogsContainer)
        txtEmptyLogs = findViewById(R.id.txtEmptyLogs)
        txtStatWorkouts = findViewById(R.id.txtStatWorkouts)
        txtStatMinutes = findViewById(R.id.txtStatMinutes)
        txtStatCalories = findViewById(R.id.txtStatCalories)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnSetGoal).setOnClickListener { showGoalDialog(null) }
        findViewById<TextView>(R.id.btnEditGoal).setOnClickListener { showGoalDialog(currentGoal) }
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabOpenMap)
            .setOnClickListener {
                startActivity(Intent(this, MapActivity::class.java))
            }

        setupActivityTypesRecyclerView()
        loadData()
    }

    private fun setupActivityTypesRecyclerView() {
        activityTypeAdapter = ActivityTypeAdapter { activity ->
            openLogWorkout(activity.activityKey)
        }

        rvActivityTypes = findViewById(R.id.rvActivityTypes)
        rvActivityTypes.apply {
            layoutManager = object : LinearLayoutManager(this@ActivitiesActivity) {
                override fun canScrollVertically(): Boolean = false
            }
            adapter = activityTypeAdapter
            isNestedScrollingEnabled = false
            setHasFixedSize(false)
            itemAnimator = null
            activityTypeAdapter.registerAdapterDataObserver(
                object : RecyclerView.AdapterDataObserver() {
                    override fun onChanged() = rvActivityTypes.expandToFitAllItems()
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) =
                        rvActivityTypes.expandToFitAllItems()
                },
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (!SessionManager.isLoggedIn()) {
            finish()
            return
        }
        if (::repository.isInitialized) {
            loadData()
        }
    }

    private fun loadData() {
        val email = SessionManager.getUserEmail()

        lifecycleScope.launch {
            val activities = activityRepository.getActivities()
            activityTypeAdapter.submitList(activities)
            rvActivityTypes.expandToFitAllItems()

            val stats = repository.getWeeklyStats(email)
            txtStatWorkouts.text = stats.workoutCount.toString()
            txtStatMinutes.text = stats.totalMinutes.toString()
            txtStatCalories.text = stats.totalCalories.toString()

            val goalStatus = goalRepository.getTodayGoalStatus(email)
            renderTodayGoal(
                goal = goalStatus.goal,
                burnedCalories = goalStatus.burnedCalories,
                progressPercent = goalStatus.progressPercent,
                isComplete = goalStatus.isComplete,
            )

            val logs = repository.getRecentWorkouts(email)
            renderRecentLogs(logs)
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
            else -> {
                val remaining = (goal.targetCalories - burnedCalories).coerceAtLeast(0)
                getString(
                    R.string.home_goal_progress,
                    burnedCalories,
                    goal.targetCalories,
                ) + " · " + getString(R.string.home_goal_percent, progressPercent) +
                    " · " + getString(R.string.activities_goal_remaining, remaining)
            }
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
                    Toast.makeText(this@ActivitiesActivity, R.string.home_goal_saved, Toast.LENGTH_SHORT).show()
                    loadData()
                } else {
                    Toast.makeText(
                        this@ActivitiesActivity,
                        result.exceptionOrNull()?.message ?: getString(R.string.log_error_save),
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
        }
    }

    private fun renderRecentLogs(logs: List<WorkoutEntity>) {
        recentLogsContainer.removeAllViews()

        if (logs.isEmpty()) {
            txtEmptyLogs.visibility = View.VISIBLE
            return
        }

        txtEmptyLogs.visibility = View.GONE
        val inflater = LayoutInflater.from(this)
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        logs.forEach { workout ->
            val type = ActivityType.fromKey(workout.activityType) ?: ActivityType.CARDIO
            val itemView = inflater.inflate(R.layout.item_workout_log, recentLogsContainer, false)

            itemView.findViewById<TextView>(R.id.txtLogEmoji).text = type.emoji
            itemView.findViewById<TextView>(R.id.txtLogTitle).text = getString(type.labelRes)
            itemView.findViewById<TextView>(R.id.txtLogDetails).text = formatLogDetails(workout)
            itemView.findViewById<TextView>(R.id.txtLogDate).text =
                dateFormat.format(Date(workout.loggedAt))

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                bottomMargin = dp(8)
            }
            recentLogsContainer.addView(itemView, params)
        }
    }

    private fun formatLogDetails(workout: WorkoutEntity): String {
        val parts = mutableListOf(getString(R.string.log_detail_duration, workout.durationMinutes))
        workout.weightKg?.let {
            parts.add(getString(R.string.log_detail_weight, it))
        }
        workout.calories?.let {
            parts.add(getString(R.string.log_detail_calories, it))
        }
        return parts.joinToString(" · ")
    }

    private fun openLogWorkout(activityKey: String) {
        startActivity(
            Intent(this, LogWorkoutActivity::class.java).apply {
                putExtra(LogWorkoutActivity.EXTRA_ACTIVITY_TYPE, activityKey)
            },
        )
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
