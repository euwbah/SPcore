package com.spcore.backend

import com.spcore.activities.LoginActivity.LoginStatus
import com.spcore.helpers.HARDCODE_MODE
import com.spcore.helpers.backendErrorAdapter

/**
 * A collection of synchronous methods which convert raw data from backend into
 * however the frontend needs to implement it.
 *
 * These functions abstract out HARDCODE_MODE from implementation so
 * the view controllers will not need to do so
 *
 * REMEMBER TO CALL THESE FROM A BACKGROUND THREAD IF NOT THE APP WILL CLOSE
 */
object FrontendInterface {
    fun performLogin(adminNo: String, password: String) : LoginStatus {
        if (HARDCODE_MODE) {
            val fakeResponse = LoginResponse("Fake JWT Token")
            return when (adminNo) {
                "1234567" -> LoginStatus.SUCCESS(fakeResponse)
                "7654321" -> LoginStatus.SP_SERVER_DOWN
                else -> LoginStatus.INVALID_CREDENTIALS
            }
        }

        val resp = Backend.performLogin(adminNo, password).execute()

        if(!resp.isSuccessful) {
            resp.errorBody()?.string()?.let {
                val err = backendErrorAdapter.fromJson(it)

                err?.code?.let {
                    return when(it) {
                        2 -> LoginStatus.INVALID_CREDENTIALS
                        3 -> LoginStatus.SP_SERVER_DOWN
                        else -> LoginStatus.UNEXPECTED_ERROR(it, err.message)
                    }
                }
            }

            return LoginStatus.VOID
        }

        resp.body()?.let {
            return LoginStatus.SUCCESS(it)
        }

        return LoginStatus.VOID
    }

    // NOTE: month is 1-based
    fun getTimetable(year: Int, month: Int) {

    }
}