package com.spcore.spmobileapi.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// http://mobileappnew.sp.edu.sg/spTimetable/source/sptt.php?DDMMYY=231017&id=1626498

/**
 * ## REST interface for use with the Retrofit HTTP API
 */
internal interface SPAPIRestCalls {
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