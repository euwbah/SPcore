package com.spcore.helpers

import android.content.Context
import android.content.SharedPreferences
import com.spcore.R
import com.spcore.models.Lesson

/**
 * Call this once during the splash screen
 */
fun <T : Context> T.initSharedPrefs() {
    arrayOf(Auth, ATS)
            .map { it.initializeSP(this)}
}

interface SharedPrefWrapper {
    fun <T : Context> initializeSP(context: T)
}

object Auth : SharedPrefWrapper {
    private lateinit var jwtSP : SharedPreferences

    override fun <T : Context> initializeSP(context: T) {
        if(!this@Auth::jwtSP.isInitialized)
            jwtSP = context.getSharedPreferences(
                    context.getString(R.string.jwt_token_shared_preference_id),
                    Context.MODE_PRIVATE)
    }

    /**
     * ### REMEMBER TO CALL `Context.initJWTTokenSP()` first!
     */
    fun getJwtToken() : String? {
        if (!this::jwtSP.isInitialized)
            throw UnsupportedOperationException("JWT Token shared preferences not initialized yet!")

        return jwtSP.getString("token", null)
    }

    /**
     * ### REMEMBER TO CALL `Context.initJWTTokenSP()` first!
     */
    fun setJwtToken(token: String) {
        if (!this::jwtSP.isInitialized)
            throw UnsupportedOperationException("JWT Token shared preferences not initialized yet!")

        jwtSP
                .edit()
                .putString("token", token)
                .apply()
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