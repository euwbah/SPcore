package com.spcore.activities

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.util.Log


@SuppressLint("Registered") // no need to register as it is a super type
/**
 * A superclass for activities that helps to keep track of the application state
 * based on activities' [AppStateTrackerActivity.onPause] and [onResume]
 *
 * @param activitySPIdentity A unique string to identify the activity
 */
open class AppStateTrackerActivity(private val activitySPIdentity: String) : AppCompatActivity() {
    override fun onResume() {
        super.onResume()

        Log.d("RESUMED", activitySPIdentity)

        getSharedPreferences("com.spcore.appstate", Context.MODE_PRIVATE)
                .edit()
                .putString("active", activitySPIdentity)
                .apply()
    }

    override fun onPause() {
        super.onPause()

        Log.d("PAUSED", activitySPIdentity)

        getSharedPreferences("com.spcore.appstate", Context.MODE_PRIVATE)
                .edit()
                .putString("active", "none")
                .apply()
    }
}