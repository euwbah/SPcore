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
    private var authSP: SharedPreferences? = null

    override fun <T : Context> initializeSP(context: T) {
        if(authSP == null)
            authSP = context.getSharedPreferences(
                    context.getString(R.string.jwt_token_shared_preference_id),
                    Context.MODE_PRIVATE)
    }

    /**
     * ### REMEMBER TO CALL `Context.initJWTTokenSP()` first!
     */
    fun getJwtToken() : String? {
        if(authSP == null)
            throw UnsupportedOperationException("JWT Token shared preferences not initialized yet!")

        return authSP?.getString("token", null)
    }

    /**
     * ### REMEMBER TO CALL `Context.initJWTTokenSP()` first!
     */
    private fun setJwtToken(token: String) {
        if(authSP == null)
            throw UnsupportedOperationException("JWT Token shared preferences not initialized yet!")

        authSP
                ?.edit()
                ?.putString("token", token)
                ?.apply()
    }

    private fun saveCredentials(adminNo: String, pass: String) {
        if (authSP == null)
            throw UnsupportedOperationException("JWT Token shared preferences not initialized yet!")

        authSP
                ?.edit()
                ?.putString("23gnoiasbrjaeorbin",
                        Base64.encode(adminNo.toByteArray(), Base64.DEFAULT)
                                .map { it.toChar().toString() }
                                .reduce { acc, c -> acc + c })
                ?.putString("argjoaierogjeoagij",
                        Base64.encode(pass.toByteArray(), Base64.DEFAULT)
                                .map { it.toChar().toString() }
                                .reduce { acc, c -> acc + c })
                ?.apply()
    }

    fun isLoggedIn() : Boolean {
        return getJwtToken() != null
    }

    /**
     * Returns a list duple. Suggested usage: **`val (adminNo, pass) = getCredentials()`**
     */
    fun getCredentials(): List<String> {
        if(authSP == null)
            throw UnsupportedOperationException("JWT Token shared preferences not initialized yet!")

        val adminNo = authSP?.getString("23gnoiasbrjaeorbin", null)
        val pass = authSP?.getString("argjoaierogjeoagij", null)

        if (adminNo _or pass _is null)
            throw NotLoggedInException()

        // can be asserted because ^^^
        return listOf(adminNo!!, pass!!).map {
            Base64.decode(it.toByteArray(), Base64.DEFAULT)
                    .map { it.toChar().toString() }
                    .reduce { acc, s -> acc + s }
        }
    }

    /**
     * Use this to mark the user as initialized locally so that the app only
     * needs to check with the server once.
     *
     * NOTE: this method, along with [getUserInitializedLocally] are abstracted by
     * [com.spcore.apis.FrontendInterface.isUserInitializedOnServer], so there is no need to
     * call these in the view controllers
     */
    fun setUserInitializedLocally(username: String, displayedName: String? = null) {
        if(authSP == null)
            throw UnsupportedOperationException("JWT Token shared preferences not initialized yet!")

        authSP
                ?.edit()
                ?.putBoolean("init", true)
                ?.putString("username", username)
                ?.putString("displayed name", displayedName)
                ?.apply()
    }

    /**
     * Use this to retrieve whether [setUserInitializedLocally] has been called
     *
     * NOTE: This method is abstracted by the FrontendInterface as this is cached data
     */
    fun getUserInitializedLocally(): Boolean {
        if(authSP == null)
            throw UnsupportedOperationException("JWT Token shared preferences not initialized yet!")

        return authSP?.getBoolean("init", false) ?: false
    }

    /**
     * Use this to retrieve the locally-stored username
     * Will only work if [setUserInitializedLocally] or [setUsername] has been
     * called prior, if not, it will return null.
     *
     * NOTE: This method is abstracted by the [com.spcore.apis.FrontendInterface] as this is cached data.
     * Upon null return, the [com.spcore.apis.FrontendInterface] will then poll the server and update the
     * locally cached values
     */
    fun getUsername() : String? {
        if(authSP == null)
            throw UnsupportedOperationException("JWT Token shared preferences not initialized yet!")

        return authSP?.getString("username", null)
    }

    /**
     * Updates the cached username of the user
     */
    fun setUsername(un: String) {
        if(authSP == null)
            throw UnsupportedOperationException("JWT Token shared preferences not initialized yet!")

        authSP
                ?.edit()
                ?.putString("username", un)
                ?.apply()
    }

    /**
     * Gets the cached displayed name of the user (note that this may be null even if the
     * user has been initialized already
     *
     * NOTE: This method is abstracted by the [com.spcore.apis.FrontendInterface] as this is cached data.
     * Upon null return, the [com.spcore.apis.FrontendInterface] will then poll the server and update the
     * locally cached values
     */
    fun getDisplayedName() : String? {
        if(authSP == null)
            throw UnsupportedOperationException("JWT Token shared preferences not initialized yet!")

        return authSP?.getString("displayed name", null)
    }

    /**
     * Updates the cached displayed name of the user
     */
    fun setDisplayedName(dn: String?) {
        if(authSP == null)
            throw UnsupportedOperationException("JWT Token shared preferences not initialized yet!")

        authSP
                ?.edit()
                ?.putString("displayed name", dn)
                ?.apply()
    }


    /**
     * Initialize all necessary session data
     */
    fun login(jwtToken: String, adminNo: String, pass: String) {
        setJwtToken(jwtToken)
        saveCredentials(adminNo, pass)
    }

    /**
     * Removes all the session data and cached data from the [authSP]
     */
    fun logout() {
        if(isLoggedIn()) {
            authSP
                    ?.edit()
                    ?.clear()
                    ?.apply()
        }
    }

}

/**
 * So far, this shared pref is just used to store whether or not the current lesson's ATS was keyed
 */
object ATS : SharedPrefWrapper {
    private var ATSSP : SharedPreferences? = null

    override fun <T : Context> initializeSP(context: T) {
        if(ATSSP == null)
            ATSSP = context.getSharedPreferences(
                    context.getString(R.string.ats_shared_preference_id),
                    Context.MODE_PRIVATE)
    }

    fun markATSSubmitted(forLesson: Lesson) {
        if(ATSSP == null)
            throw UnsupportedOperationException("ATS shared preferences not initialized yet")

        ATSSP
                ?.edit()
                ?.putString("lessonID", forLesson.id.toString())
                ?.apply()
    }

    fun checkATSSubmitted(currLesson: Lesson) : Boolean {
        if(ATSSP == null)
            throw UnsupportedOperationException("ATS shared preferences not initialized yet")

        return ATSSP?.getString("lessonID", "anignatup") == currLesson.id.toString()
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
    private var AppStateSP: SharedPreferences? = null

    override fun <T : Context> initializeSP(context: T) {
        if(AppStateSP == null)
            AppStateSP = context.getSharedPreferences(
                    "com.spcore.appstate",
                    Context.MODE_PRIVATE)
    }

    /**
     * Get's the activity ID of the current foreground activity as provided in [com.spcore.activities.AppStateTrackerActivity]
     * or "none" if none are in the foreground
     *
     * @return Returns the activity ID or "none" if none are in the foreground
     */
    fun getForegroundActivity() : String {
        if(AppStateSP == null)
            throw UnsupportedOperationException("AppState shared pref not init yet")

        return AppStateSP?.getString("active", "none")  ?: "none"
    }

    infix fun foregroundIs(activityID: String): Boolean {
        return getForegroundActivity() == activityID
    }

    infix fun foregroundIsnt(activityID: String): Boolean {
        return getForegroundActivity() != activityID
    }

    /**
     * If compared with [String], will use [foregroundIs]
     */
    override fun equals(other: Any?): Boolean {
        return if (other is String)
            foregroundIs(other)
        else
            super.equals(other)
    }
}