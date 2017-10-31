/**
 * Retrofit REST client interface
 *  **IMPORTANT: All calls returned are synchronous
 * Android asserts asynchronicity of all HTTP calls, and must be
 * implemented at a higher level than the calls here in this package
 */
package com.spcore.spmobileapi.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

// http://mobileappnew.sp.edu.sg/spTimetable/source/sptt.php?DDMMYY=231017&id=1626498

/**
 * ## REST interface for use with the Retrofit HTTP API
 */
internal interface SPMobileAppRESTInterface {
    @GET("spTimetable/source/sptt.php")
    fun getTimetable(
            @Query("DDMMYY") ddmmyy: String,
            @Query("id") studentPID: Int) : Call<TimetableDayResponse>

    @GET("/spcalendar/calendar2.json")
    fun getCalendar(
            // Honestly these parameters are useless
            @Query("_dc") _dc: String? = null,
            @Query("limit") limit: Int = 25) : Call<CalendarResponse>



}

internal interface ATSRestInterface {

    /*
        Step 0: Assuming MYATS_80_PORTAL_PSJSESSIONID cookie doesn't exist
            GET https://myats.sp.edu.sg/
            Save cookies:
                MYATS-80-PORTAL-PSJSESSIONID
            Expected Response:
                Code 302: Redirect to URL in "Location" response header

        Step 1:
            REDIRECT GET https://myats.sp.edu.sg/psc/cs90atstd/EMPLOYEE/HRMS/c/A_STDNT_ATTENDANCE.A_ATS_STDNT_SBMIT.GBL
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
            REDIRECT GET ** same url as Location header received in Step 2 **
            Send Header:
                Referer: ** Same url as Location header in Step 2 **
            Send cookies:
                everything
            Save cookies:
                PS_TOKENEXPIRE (new value)
            Expected Response:
                Code 200: Html file containing hidden input fields for use in Step 4

        Step 4:
            POST https://myats.sp.edu.sg/psc/cs90atstd/EMPLOYEE/HRMS/c/A_STDNT_ATTENDANCE.A_ATS_STDNT_SBMIT.GBL
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


    /**
     * As pointless this function looks, it is actually necessary to call this
     * before `step2_3()` to get dem cookies
     *
     * All the magic happens behind the scenes with the Interceptors
     */
    @GET("/")
    fun step0_1() : Call<ResponseBody>

    /**
     * More stuff happening here
     * The inline-query params `cmd=login&language=ENG` are statically
     * built-in into this call
     *
     * Should the returned call have errorCode=105 in the redirected url,
     * the credentials the user entered were invalid
     *
     * @param body The user's credentials in the form of the `ATSLoginBody` data model
     */
    @FormUrlEncoded
    @POST("/psc/cs90atstd/EMPLOYEE/HRMS/c/A_STDNT_ATTENDANCE.A_ATS_STDNT_SBMIT.GBL" +
            "?cmd=login&language=ENG")
    fun step2_3(
            @Field("userid") userid: String, @Field("pwd") pwd: String, @Field("timezoneOffset") timezoneOffset: Int = -480) : Call<ResponseBody>

    /**
     * @param body A HashMap containing all the hidden input fields from the HTML
     *             response in Step3
     */
    @FormUrlEncoded
    @POST("/psc/cs90atstd/EMPLOYEE/HRMS/c/A_STDNT_ATTENDANCE.A_ATS_STDNT_SBMIT.GBL")
    fun step4(
            @FieldMap body: Map<String, String>) : Call<ResponseBody>

}