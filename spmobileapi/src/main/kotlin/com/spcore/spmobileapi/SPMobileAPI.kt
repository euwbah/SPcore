@file:Suppress("ObjectPropertyName")

package com.spcore.spmobileapi

import com.spcore.spmobileapi.Result.*
import com.spcore.spmobileapi.api.SPAPIRestCalls
import com.spcore.spmobileapi.api.ServerResponseException
import com.spcore.spmobileapi.api.TimetableDayResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.SocketTimeoutException

/**
 * A Kotlin Singleton. This is the main exposure point of the API.
 */
object SPMobileAPI {
    // Local API server interface (Unicode+1f5a5: 🖥)
    private val restCalls: SPAPIRestCalls

    init {

        val retrofit =
                Retrofit.Builder()
                        .baseUrl("http://mobileappnew.sp.edu.sg")
                        .addConverterFactory(MoshiConverterFactory.create())
                        .build()

        restCalls = retrofit.create(SPAPIRestCalls::class.java)
    }

    // FIXME: Remember to set to internal before release
    fun getTimetableDay(ddmmyy: String, ID: Int) : Result {
        val response: Response<TimetableDayResponse>

        try {
            response = restCalls.getTimetable(ddmmyy, ID).execute()
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
        val response = restCalls.getCalendar().execute()

        return if(response.isSuccessful)
            response.body().toString()
        else
            response.errorBody()?.string() ?: "Error without body"
    }

    // FIXME: Remember to set to internal before release
    fun sendATS(ID: Int, pass: String, ats: Int) {
       /*
            Step 0: Assuming MYATS_80_PORTAL_PSJSESSIONID cookie doesn't exist
                GET https://myats.sp.edu.sg/
                Save headers:
                    Location
                Save cookies:
                    MYATS-80-PORTAL-PSJSESSIONID
                Expected Response:
                    Code 302: Redirect to URL in "Location" response header
            Step 1:
                GET https://myats.sp.edu.sg/psc/cs90atstd/EMPLOYEE/HRMS/c/A_STDNT_ATTENDANCE.A_ATS_STDNT_SBMIT.GBL
                Inline query:
                    cmd = login
                Send cookies:
                    MYATS-80-PORTAL-PSJSESSIONID (if stored locally)
                Save cookies:
                    MYATS-80-PORTAL-PSJSESSIONID (if not yet stored locally / the server gives a new one)
                    PS_TOKEN
                Expected Response:
                    Code 200: Responds with HTML login page

            Step 2:
                POST https://myats.sp.edu.sg/psc/cs90atstd/EMPLOYEE/HRMS/c/A_STDNT_ATTENDANCE.A_ATS_STDNT_SBMIT.GBL
                Inline query:
                    cmd = login
                    language = ENG
                Body (form data):
                    timezoneOffset = -480 (GMT + 8 - 8 = 0)
                    userid: pxxxxxxx
                    pwd: Elmo'sWorld2
                Send cookies:
                    MYATS-80-PORTAL-PSJSESSIONID
                Save cookies:
                    ExpirePage
                    MYATS-80-PORTAL-PSJSESSIONID (new value)
                    PS_LOGINLIST
                    PS_TOKENEXPIRE
                    PS_TOKEN
                    SignOnDefault
                Save header:
                    Location
                Expected Response:
                    Code 302: Send redirect link via "Location" header
                Abnormal Response:
                    Code 302: "Location" header redirect link has inline-query params:
                        errorCode=105
                    Response header has
                        RespondingWithSignonPage = true
                    Resulting request:
                        GET https://myats.sp.edu.sg/psc/cs90atstd/EMPLOYEE/HRMS/c/A_STDNT_ATTENDANCE.A_ATS_STDNT_SBMIT.GBL?&cmd=login&errorCode=105&languageCd=ENG
                    Resulting Expected Response:
                        Code 200: Html file containing login screen with phrase "Your User ID and/or Password are invalid."

            Step 3:
                GET ** same url as Location header received in Step 2 **
                Send Header:
                    Referer: ** Same url as Location header in Step 2 **
                Send cookies:
                    everything
                Save cookies:
                    PS_TOKENEXPIRE (new value)
                Save ALL hidden input fields for use in Step 4

            Step 4:
                https://myats.sp.edu.sg/psc/cs90atstd/EMPLOYEE/HRMS/c/A_STDNT_ATTENDANCE.A_ATS_STDNT_SBMIT.GBL
                Body (form data):
                    Send ALL hidden input fields from step 3
                    A_ATS_ATCD_SBMT_A_ATS_ATTNDNCE_CD: ** ats code **
                Send Cookies:
                    Everything
                Save Cookies:
                    PS_TOKENEXPIRE
                Expected response:
                    Code 200 && presence of the word "successfully" in the returned XML response
                Abnormal response:
                    Presence of word "already" in the returned XML response:
                        ATS code already sent
                    Presence of word "Invalid" in the returned XML response:
                        ATS code is invalid



        */

       // Step 1:


   }
}
