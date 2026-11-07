package com.example.register

import android.content.Intent
import com.example.register.model.ActivityType

object NavigationHelper {

    fun openActivities(from: MainActivity) {
        from.startActivity(Intent(from, ActivitiesActivity::class.java))
    }

    fun openLogWorkout(from: MainActivity, type: ActivityType) {
        from.startActivity(
            Intent(from, LogWorkoutActivity::class.java).apply {
                putExtra(LogWorkoutActivity.EXTRA_ACTIVITY_TYPE, type.key)
            },
        )
    }

    fun openProfile(from: MainActivity) {
        from.startActivity(Intent(from, ProfileActivity::class.java))
    }
}
