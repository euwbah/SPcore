package com.spcore.helpers

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.spcore.R
import com.spcore.exceptions.NotLoggedInException
import com.spcore.models.Lesson

/**
 * Call this once during the splash screen
 */
fun <T : Context> T.initSharedPrefs() {
    arrayOf(Auth, ATS, AppState)
            .map { it.initializeSP(this)}
}

interface SharedPrefWrapper {
    fun <T : Context> initializeSP(context: T)
}

object Auth : SharedPrefWrapper {
    private lateinit var authSP: SharedPreferences

    override fun <T : Context> initializeSP(context: T) {
        if(!this@Auth::authSP.isInitialized)
            authSP = context.getSharedPreferences(
                    context.getString(R.string.jwt_token_shared_preference_id),
                    Context.MODE_PRIVATE)
    }

    /**
     * ### REMEMBER TO CALL `Context.initJWTTokenSP()` first!
     */
    fun getJwtToken() : String? {
        if (!this::authSP.isInitialized)
            throw UnsupportedOperationException("JWT Token shared preferences not initialized yet!")

        return authSP.getString("token", null)
    }

    /**
     * ### REMEMBER TO CALL `Context.initJWTTokenSP()` first!
     */
    fun setJwtToken(token: String) {
        if (!this::authSP.isInitialized)
            throw UnsupportedOperationException("JWT Token shared preferences not initialized yet!")

        authSP
                .edit()
                .putString("token", token)
                .apply()
    }

    fun saveCredentials(adminNo: String, pass: String) {
        if (!this::authSP.isInitialized)
            throw UnsupportedOperationException("JWT Token shared preferences not initialized yet!")

        authSP
                .edit()
                .putString("23gnoiasbrjaeorbin", Base64.encode(adminNo.toByteArray(), Base64.DEFAULT).toString())
                .putString("argjoaierogjeoagij", Base64.encode(pass.toByteArray(), Base64.DEFAULT).toString())
                .apply()
    }

    /**
     * Returns a list duple. Suggested usage: **`val (adminNo, pass) = getCredentials()`**
     */
    fun getCredentials(): List<String> {
        if (!this::authSP.isInitialized)
            throw UnsupportedOperationException("JWT Token shared preferences not initialized yet!")

        val adminNo = authSP.getString("23gnoiasbrjaeorbin", null)
        val pass = authSP.getString("argjoaierogjeoagij", null)

        if (adminNo _or pass _is null)
            throw NotLoggedInException()

        return listOf(adminNo, pass)
    }

}

/**
 * So far, this shared pref is just used to store whether or not the current lesson's ATS was keyed
 */
object ATS : SharedPrefWrapper {
    private lateinit var ATSSP : SharedPreferences

    override fun <T : Context> initializeSP(context: T) {
        if(!this@ATS::ATSSP.isInitialized)
            ATSSP = context.getSharedPreferences(
                    context.getString(R.string.ats_shared_preference_id),
                    Context.MODE_PRIVATE)
    }

    fun markATSSubmitted(forLesson: Lesson) {
        if(!this@ATS::ATSSP.isInitialized)
            throw UnsupportedOperationException("ATS shared preferences not initialized yet")

        ATSSP
                .edit()
                .putString("lessonID", forLesson.id.toString())
                .apply()
    }

    fun checkATSSubmitted(currLesson: Lesson) : Boolean {
        if(!this@ATS::ATSSP.isInitialized)
            throw UnsupportedOperationException("ATS shared preferences not initialized yet")

        return ATSSP.getString("lessonID", "anignatup") == currLesson.id.toString()
    }
}

/**
 * Used to get the state of the app (i.e., what activities are running now)
 *
 * It's purpose is to evaluate the need for sending a notification on ATS submission status &em;
 * *e.g.*:
 * if the LDA is running:
 * - upon successful entry ==> display toast message
 * - upon erroneous entry  ==> reshow the [com.spcore.fragments.ATSEntryDialogFragment] dialog
 *
 * if the LDA isn't running:
 * - upon successful entry ==> display notification, when that is clicked, it opens up the lesson
 *   that ATS was entered for
 * - upon erroneous entry  ==> display relevant notification with quick-reply which allows user to retype ATS,
 *   in the scenario of not being connected to school wifi, there will be no quick-reply.
 *   When the notification is clicked, it opens up the ATS submission dialog
 */
object AppState : SharedPrefWrapper {
    private lateinit var AppStateSP: SharedPreferences

    override fun <T : Context> initializeSP(context: T) {
        if(!this::AppStateSP.isInitialized)
            AppStateSP = context.getSharedPreferences(
                    "com.spcore.appstate",
                    Context.MODE_PRIVATE)
    }

    /**
     * Get's the activity ID of the current foreground activity as provided in [com.spcore.activities.AppStateTrackerActivity]
     * or "none" if none are in the foreground
     */
    fun getForegroundActivity() : String {
        if(!this::AppStateSP.isInitialized)
            throw UnsupportedOperationException("AppState shared pref not init yet")

        return AppStateSP.getString("active", "none")
    }

}