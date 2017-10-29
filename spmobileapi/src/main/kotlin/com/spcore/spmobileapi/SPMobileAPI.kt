@file:Suppress("ObjectPropertyName")

package com.spcore.spmobileapi

import android.content.SharedPreferences
import com.spcore.spmobileapi.Result.*
import com.spcore.spmobileapi.UnexpectedAPIException.UnexpectedAPIError.*
import com.spcore.spmobileapi.api.ATSLoginBody
import com.spcore.spmobileapi.api.ATSRestInterface
import com.spcore.spmobileapi.api.SPMobileAppRESTInterface
import com.spcore.spmobileapi.api.ServerResponseException
import com.spcore.spmobileapi.helpers.CookieStore
import com.spcore.spmobileapi.helpers.CookiesAddInterceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.SocketException
import java.net.SocketTimeoutException


/**
 * A Kotlin Singleton. This is the main exposure point of the API.
 */
object SPMobileAPI {
    // Local API server interface (Unicode+1f5a5: 🖥)
    private lateinit var mobileAppCalls: SPMobileAppRESTInterface
    private lateinit var atsCalls: ATSRestInterface

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

        atsCalls = newRetrofit(
                "https://myats.sp.edu.sg",
                ATSRestInterface::class.java)

        this.isInitalized = true
    }

    private fun assertInitialization() {
        if (!isInitalized)
            throw SPMobileAPIUninitializedException()
    }









    /**
     * @throws NoInternetException
     * @throws ErroneousResponseException
     */
    // FIXME: Remember to set to internal before release
    fun getTimetableDay(ddmmyy: String, ID: Int) : Day {

        assertInitialization()

        val response =
                try { mobileAppCalls.getTimetable(ddmmyy, ID).execute() }
                catch (e: SocketTimeoutException) { throw NoInternetException() }

        if (!response.isSuccessful)
            throw ErroneousResponseException(response.errorBody())



        return response.body()?.let { Day(it, ddmmyy) }
                ?: throw UnexpectedAPIException(NO_RESPONSE_BODY)
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


    /**
     * @throws
     */
    fun sendATS(ID: String, pass: String, ats: Int): ATSResult {

        assertInitialization()

        val id = (if (ID.toLowerCase().startsWith("p")) "" else "p") + ID

        try {

            // FIXME: Assumption made that step 0 will automatigically redirect to step 1
            // Even then, very little moving parts here, just one redirect and two
            // passes of cookie collection
            val response0_1 = atsCalls.ATS_Step0_Step1().execute()

            if (!response0_1.isSuccessful)
                return ATSResult.CONNECTION_ERROR(response0_1.errorBody())

            response0_1.raw().body()?.let {
                val htmlresponse = it.string()
                if (htmlresponse.contains("Please connect to SPStudent"))
                    return ATSResult.NOT_CONNECTED_TO_SCHOOL_WIFI
            } ?: run {
                throw UnexpectedAPIException(NO_RESPONSE_BODY)
            }

            val response2_3 =
                    atsCalls.ATS_Step2_Step3(ATSLoginBody(id, pass)).execute()

            if(!response2_3.isSuccessful)
                return ATSResult.CONNECTION_ERROR(response2_3.errorBody())

            // Step 2 failure check: ?errorCode=105
            //      => Invalid credentials
            response2_3.raw().request().url().queryParameter("errorCode")?.let {
                if (it == "105")
                    return ATSResult.INVALID_CREDENTIALS
            }

            response2_3.raw().body()?.let {

            }

        } catch (e: SocketException) {
            return ATSResult.NO_INTERNET("Socket exception")
        }
    }

    sealed class ATSResult {
        object NO_INTERNET : ATSResult()
        object NOT_CONNECTED_TO_SCHOOL_WIFI : ATSResult()
        object INVALID_CREDENTIALS : ATSResult()
        class CONNECTION_ERROR(error: ResponseBody?) : ATSResult()

    }

}
