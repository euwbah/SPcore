@file:Suppress("ObjectPropertyName")

package com.spcore.spmobileapi

import android.content.SharedPreferences
import com.spcore.spmobileapi.UnexpectedAPIException.UnexpectedAPIError.*
import com.spcore.spmobileapi.api.ATSLoginBody
import com.spcore.spmobileapi.api.ATSRestInterface
import com.spcore.spmobileapi.api.SPMobileAppRESTInterface
import com.spcore.spmobileapi.helpers.CookieStore
import com.spcore.spmobileapi.helpers.CookiesAddInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import org.jsoup.Jsoup
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


/**
 * A Kotlin Singleton. This is the main exposure point of the API.
 */
object SPMobileAPI {
    private lateinit var mobileAppCalls: SPMobileAppRESTInterface
    private lateinit var atsCalls: ATSRestInterface

    private var isInitialized = false

    fun inititialize(cookieSP: SharedPreferences) {
        if (this.isInitialized) return

        val cookieStore = CookieStore(cookieSP)
        val okhttpclient =
                OkHttpClient
                        .Builder()
                        .addInterceptor(CookiesAddInterceptor(cookieStore))
                        .build()

        fun <E> newRetrofit(baseUrl: String, interfaceType: Class<E>) =
                Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .client(okhttpclient)
                    .build().create(interfaceType)

        mobileAppCalls = newRetrofit(
                "http://mobileappnew.sp.edu.sg",
                SPMobileAppRESTInterface::class.java)

        atsCalls = newRetrofit(
                "https://myats.sp.edu.sg",
                ATSRestInterface::class.java)

        this.isInitialized = true
    }

    private fun assertInitialization() {
        if (!isInitialized)
            throw SPMobileAPIUninitializedException()
    }









    /**
     * @throws NoInternetException
     * @throws ErroneousResponseException
     */
    // FIXME: Remember to set to internal before release
    fun getTimetableDay(ddmmyy: String, ID: Int) : Result<Day, Day.Errors> {

        assertInitialization()

        val response =
                try { mobileAppCalls.getTimetable(ddmmyy, ID).execute() }
                catch (e: SocketTimeoutException) { throw NoInternetException() }
                catch (e: UnknownHostException) { throw NoInternetException() }

        if (!response.isSuccessful)
            throw ErroneousResponseException(response.errorBody())



        return response.body()?.let { Day.wrapAsResult { Day(it, ddmmyy) }}
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
    fun sendATS(ID: String, pass: String, ats: Int): Result<Nothing?, ATSResult.Errors> {

        assertInitialization()

        val id = (if (ID.toLowerCase().startsWith("p")) "" else "p") + ID

        try {

            // STEP 0 & 1 __________________________________________________________________



            // FIXME: Assumption made that step 0 will automatigically redirect to step 1
            // Even then, very little moving parts here, just one redirect and two
            // passes of cookie collection
            val response0_1 = atsCalls.step0_1().execute()

            if (!response0_1.isSuccessful)
                throw ErroneousResponseException(response0_1.errorBody())

            response0_1.body()?.let {
                val htmlresponse = it.string()
                if (htmlresponse.contains("Please connect to SPStudent"))
                    return ATSResult.wrapAsResult {
                        ATSResult.Errors.NOT_CONNECTED_TO_SCHOOL_WIFI
                    }
            } ?: run {
                throw UnexpectedAPIException(NO_RESPONSE_BODY)
            }




            // STEP 2 & 3 ____________________________________________________________________



            val response2_3 =
                    atsCalls.step2_3(ATSLoginBody(id, pass)).execute()

            if(!response2_3.isSuccessful)
                throw ErroneousResponseException(response2_3.errorBody())

            // Step 2 failure check: ?errorCode=105
            //      => Invalid credentials
            response2_3.raw().request().url().queryParameter("errorCode")?.let {
                if (it == "105")
                    return ATSResult.wrapAsResult {
                        ATSResult.Errors.INVALID_CREDENTIALS
                    }
            }

            val step4RequestBody = HashMap<String, String>()

            // Hidden input field data extraction

            response2_3.body()?.let {
                val htmlresponse = it.string()
                val document = Jsoup.parse(htmlresponse)
                val hiddenInputFields = document.select("form[name='win0'] > input[type='hidden']")
                hiddenInputFields.forEach {
                    step4RequestBody.put(it.attr("name"), it.attr("value"))
                }
            } ?: run {
                throw UnexpectedAPIException(NO_RESPONSE_BODY)
            }

            step4RequestBody.put("A_ATS_ATCD_SBMT_A_ATS_ATTNDNCE_CD", ats.toString())



            // STEP 4 _____________________________________________________________________


            val response4 = atsCalls.step4(step4RequestBody).execute()

            if (!response4.isSuccessful)
                throw ErroneousResponseException(response4.errorBody())

            response4.body()?.let {
                val xmlstr = it.string()
                return when {
                    xmlstr.contains("successfully", true) ->
                        // Note that "success" here is just a placeholder for any value
                        // other than a type of the ATSResult.Errors monad
                        ATSResult.wrapAsResult { "success" }
                    xmlstr.contains("already", true) ->
                        ATSResult.wrapAsResult {
                            ATSResult.Errors.ALREADY_ENTERED
                        }
                    else -> ATSResult.wrapAsResult {
                        ATSResult.Errors.INVALID_CODE
                    }
                }
            } ?: run {
                throw UnexpectedAPIException(NO_RESPONSE_BODY)
            }

        } catch (e: SocketException) {
            return ATSResult.wrapAsResult {
                ATSResult.Errors.NO_INTERNET
            }
        }
    }
}
