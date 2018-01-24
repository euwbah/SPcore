@file:Suppress("ObjectPropertyName")

package com.spcore.spmobileapi

import android.content.SharedPreferences
import android.util.Log
import com.spcore.spmobileapi.UnexpectedAPIException.UnexpectedAPIError.*
import com.spcore.spmobileapi.api.ATSRestInterface
import com.spcore.spmobileapi.api.SPMobileAppRESTInterface
import com.spcore.spmobileapi.interceptors.CookieStore
import com.spcore.spmobileapi.interceptors.CookiesAddInterceptor
import com.spcore.spmobileapi.interceptors.CookiesRecInterceptor
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
    private lateinit var cookieStore: CookieStore

    fun inititialize(cookieSP: SharedPreferences) {
        if (this.isInitialized) return

        cookieStore = CookieStore(cookieSP)
        val okhttpclient =
                OkHttpClient
                        .Builder()
                        .followRedirects(false)
                        .addInterceptor(CookiesAddInterceptor(cookieStore))
                        .addInterceptor(CookiesRecInterceptor(cookieStore))
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










    // TODO: complete implementation of `getCalendar()`
    fun getCalendar(): String {

        assertInitialization()

        val response = mobileAppCalls.getCalendar().execute()

        return if(response.isSuccessful)
            response.body().toString()
        else
            response.errorBody()?.string() ?: "Error without body"
    }


    /**
     * ==**THIS IS A SYNCHRONOUS FUNCTION!!! DO NOT RUN ON UI THREAD!!!**==
     *
     * NOTE: it is ok for ats to be an Int as it will be automatically 0-padded when converted
     *       a string
     */
    fun sendATS(ID: String, pass: String, ats: String): Result<Nothing?, ATSResult.Errors> {

        assertInitialization()

        val id = (if (ID.toLowerCase().startsWith("p")) "" else "p") + ID

        // Reset all cookies
        cookieStore.clearCookies()

        try {
            return ATSResult.wrapAsResult {
                Log.d("ME STEP 0_1", "============================================")
                // STEP 0 & 1 __________________________________________________________________


                // FIXME: Possible failure: assuming step 0 will automagically redirect to step 1
                // Even then, very little moving parts here, just one redirect and two
                // passes of cookie collection
                val response0_1 = atsCalls.step0_1().execute()

                if (!response0_1.isSuccessful)
                    throw ErroneousResponseException(response0_1.errorBody())

                response0_1.body()?.let {
                    val htmlresponse = it.string()
                    if (htmlresponse.contains("Please connect to SPStudent"))
                        return@wrapAsResult ATSResult.Errors.NOT_CONNECTED_TO_SCHOOL_WIFI
                } ?: run {
                    throw UnexpectedAPIException(NO_RESPONSE_BODY)
                }


                Log.d("ME STEP 2_3", "============================================")
                // STEP 2 & 3 ____________________________________________________________________


                val response2_3 =
                        atsCalls.step2_3(id, pass).execute()

                if (!response2_3.isSuccessful)
                    throw ErroneousResponseException(response2_3.errorBody())

                // Step 2 failure check: ?errorCode=105
                //      => Invalid credentials
                // FIXME: Possible point of failure in `.raw()`
                response2_3.raw().request().url().queryParameter("errorCode")?.let {
                    if (it == "105")
                        return@wrapAsResult ATSResult.Errors.INVALID_CREDENTIALS
                }

                val step4RequestBody = HashMap<String, String>()

                // Hidden input field data extraction

                response2_3.body()?.let {
                    val htmlresponse = it.string()
                    val document = Jsoup.parse(htmlresponse)
                    val hiddenInputFields = document.select("input[type='hidden']")
                    hiddenInputFields.map {
                        step4RequestBody.put(it.attr("name"), it.attr("value"))
                    }
                } ?: run {
                    throw UnexpectedAPIException(NO_RESPONSE_BODY)
                }

                step4RequestBody.put("A_ATS_ATCD_SBMT_A_ATS_ATTNDNCE_CD", ats)
                step4RequestBody.put("ICAction", "A_ATS_ATCD_SBMT_SUBMIT_BTN")
                step4RequestBody.put("ICAJAX", "1")
                step4RequestBody.put("ICNAVTYPEDROPDOWN", "0")


                Log.d("ME STEP 4", "============================================")

                // STEP 4 _____________________________________________________________________


                val response4 = atsCalls.step4(step4RequestBody).execute()

                if (!response4.isSuccessful)
                    throw ErroneousResponseException(response4.errorBody())

                response4.body()?.let {
                    val xmlstr = it.string()
                    return@wrapAsResult when {
                        xmlstr.contains("successfully", true) ->
                            // Note that "success" here is just a placeholder for any value
                            // other than a type of the ATSResult.Errors monad
                            "success"
                        xmlstr.contains("already", true) ->
                            ATSResult.Errors.ALREADY_ENTERED
                        xmlstr.contains("not registered", true) -> {
                            // example match: "not registered in DIT/FT/2B/02"
                            val pattern = Regex("not registered in (\\w+/\\w+/\\d\\w/\\d+)")
                            val match = pattern.find(xmlstr)

                            val youPickedTheWrongHouseFool =
                                    match?.groups?.get(0)?.value

                            ATSResult.Errors.WRONG_CLASS(youPickedTheWrongHouseFool ?: "No class!!?? Pwo")
                        }
                        else ->
                            ATSResult.Errors.INVALID_CODE
                    }
                } ?: run {
                    throw UnexpectedAPIException(NO_RESPONSE_BODY)
                }
            } // end of yuge'ss lambda

        } catch (e: SocketException) {
            return ATSResult.wrapAsResult {
                ATSResult.Errors.NO_INTERNET
            }
        }
    }
}
