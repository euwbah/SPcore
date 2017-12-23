package com.spcore.helpers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.spcore.R


object Auth {
    private lateinit var jwtSP : SharedPreferences

    fun <T : Context> T.initJWTTokenSP() : SharedPreferences {
        if(!this@Auth::jwtSP.isInitialized)
            jwtSP = this.getSharedPreferences(
                        this.getString(R.string.jwt_token_shared_preference_id),
                        Context.MODE_PRIVATE)
        else
            Log.d("SPCORE", "JWT token unnecessarily initialized again")
        return jwtSP
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