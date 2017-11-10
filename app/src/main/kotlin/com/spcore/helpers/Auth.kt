package com.spcore.helpers

import android.content.Context
import android.content.SharedPreferences
import com.spcore.R


object Auth {
    fun <T : Context> T.retrieveJWTTokenSP() : SharedPreferences {
        return this.getSharedPreferences(
                this.getString(R.string.jwt_token_shared_preference_id),
                                      Context.MODE_PRIVATE)
    }
}