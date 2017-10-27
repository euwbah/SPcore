@file:Suppress("ObjectPropertyName")

package com.spcore.spmobileapi

import android.content.SharedPreferences
import com.spcore.spmobileapi.Result.*
import com.spcore.spmobileapi.api.ATSRestInterface
import com.spcore.spmobileapi.api.SPMobileAppRESTInterface
import com.spcore.spmobileapi.api.ServerResponseException
import com.spcore.spmobileapi.api.TimetableDayResponse
import com.spcore.spmobileapi.helpers.CookieStore
import com.spcore.spmobileapi.helpers.CookiesAddInterceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.SocketTimeoutException

/**
 * A Kotlin Singleton. This is the main exposure point of the API.
 */
object SPMobileAPI {
    // Local API server interface (Unicode+1f5a5: 🖥)
    private lateinit var mobileAppCalls: SPMobileAppRESTInterface
    private lateinit var ATSCalls: ATSRestInterface

    private var isInitalized = false

    fun inititialize(cookieSP: SharedPreferences) {
        if (this.isInitalized) return

        val cookieStore = CookieStore(cookieSP)
        val okhttpclient = OkHttpClient()
        okhttpclient.interceptors().add(CookiesAddInterceptor(cookieStore))

        fun <E> newRetrofit(baseUrl: String, interfaceType: Class<E>) =
                Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build().create(interfaceType)

        mobileAppCalls = newRetrofit(
                "http://mobileappnew.sp.edu.sg",
                SPMobileAppRESTInterface::class.java)

        ATSCalls = newRetrofit(
                "https://myats.sp.edu.sg",
                ATSRestInterface::class.java)

        this.isInitalized = true
    }

    private fun assertInitialization() {
        if (!isInitalized)
            throw SPMobileAPIUninitializedException()
    }

    // FIXME: Remember to set to internal before release
    fun getTimetableDay(ddmmyy: String, ID: Int) : Result {

        assertInitialization()

        val response: Response<TimetableDayResponse>

        try {
            response = mobileAppCalls.getTimetable(ddmmyy, ID).execute()
        } catch (e: SocketTimeoutException) {
            return NoInternet("socket timeout")
        }

        if (response.isSuccessful) {
            val timetableDay: Day?
            try {
                timetableDay = response.body()?.let { Day(it, ddmmyy) }

            } catch (ex: ServerResponseException) {
                return InvalidArguments(ex.message)
            }

            return timetableDay ?: UnexpectedError(UnexpectedErrorType.NO_RESPONSE_BODY)

        }
        else
            return ConnectionError(response.errorBody())
    }

    // FIXME: Remember to set to internal before release
    fun getCalendar(): String {

        assertInitialization()

        val response = mobileAppCalls.getCalendar().execute()

        return if(response.isSuccessful)
            response.body().toString()
        else
            response.errorBody()?.string() ?: "Error without body"
    }

    // FIXME: Remember to set to internal before release
    fun sendATS(ID: Int, pass: String, ats: Int) {

        assertInitialization()


        // Step 0:



   }
}
