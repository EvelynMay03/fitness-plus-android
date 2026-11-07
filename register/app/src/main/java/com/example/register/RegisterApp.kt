package com.example.register

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.google.android.gms.maps.MapsInitializer

class RegisterApp : Application() {

    private var startedActivityCount = 0

    override fun onCreate() {
        super.onCreate()
        MapsInitializer.initialize(applicationContext)
        SessionManager.clearSession()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

            override fun onActivityStarted(activity: Activity) {
                startedActivityCount++
            }

            override fun onActivityResumed(activity: Activity) = Unit

            override fun onActivityPaused(activity: Activity) = Unit

            override fun onActivityStopped(activity: Activity) {
                startedActivityCount--
                if (startedActivityCount == 0) {
                    SessionManager.clearSession()
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

            override fun onActivityDestroyed(activity: Activity) = Unit
        })
    }
}
