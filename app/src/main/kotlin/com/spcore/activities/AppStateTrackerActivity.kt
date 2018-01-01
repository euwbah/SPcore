package com.spcore.activities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup


@SuppressLint("Registered") // no need to register as it is a super type
/**
 * A superclass for activities that helps to keep track of the application state
 * based on activities' [AppStateTrackerActivity.onPause] and [onResume]
 *
 * @param activitySPIdentity A unique string to identify the activity
 */
abstract class AppStateTrackerActivity(private val activitySPIdentity: String) : AppCompatActivity() {

    /**
     * Use this property, [_rootView], to access the root view within this class,
     * don't use [getRootView]
     */
    private val _rootView by lazy { getRootView() }

    /**
     * This should return the root view of the current activity which is used to create Snackbars
     */
    open fun getRootView() : View {
        return findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
    }

    override fun onResume() {
        super.onResume()

        getSharedPreferences("com.spcore.appstate", Context.MODE_PRIVATE)
                .edit()
                .putString("active", activitySPIdentity)
                .apply()
    }

    override fun onPause() {
        super.onPause()

        getSharedPreferences("com.spcore.appstate", Context.MODE_PRIVATE)
                .edit()
                .putString("active", "none")
                .apply()
    }

    inner class SnackbarBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val type = intent.extras.getString("type")
            val errmsg = intent.extras.getString("errmsg")

            when(type) {
                "ats error" -> {
                    val snack = Snackbar.make(_rootView, errmsg, Snackbar.LENGTH_LONG)
                    snack.setAction("RETRY", {
                        val intent =
                                Intent(this@AppStateTrackerActivity, LessonDetailsActivity::class.java)
                                        .apply{
                                            putExtra("open ats dialog", true)
                                            putExtra("errmsg", errmsg)
                                        }

                        startActivity(intent)
                    })
                }
            }
        }
    }
}