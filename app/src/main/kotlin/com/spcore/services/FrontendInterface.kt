package com.spcore.services

import com.spcore.activities.LoginActivity.LoginStatus
import com.spcore.helpers.HARDCODE_MODE
import com.spcore.helpers.backendErrorAdapter
import com.spcore.helpers.toCalendar
import com.spcore.models.Lesson
import com.spcore.models.ScheduleItem
import java.util.*
import kotlin.collections.ArrayList

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
    fun getSchedule(year: Int, month: Int) : List<ScheduleItem> {
        val schedule = ArrayList<ScheduleItem>()

        if(HARDCODE_MODE) {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month - 1) // java's calendar's month is 0-based

            // DAY_OF_MONTH is 1-based
            val firstDayOfMonth = cal.getActualMinimum(Calendar.DAY_OF_MONTH)
            val lastDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

            for(i in firstDayOfMonth..lastDayOfMonth) {
                // for whatever reason, passing 0 to the the year param counts as the year 1900
                val start = Date(year - 1900, month - 1, i, 11, 0).toCalendar()
                val end = Date(year - 1900, month - 1, i, 13, 0).toCalendar()
                schedule.add(Lesson(
                        "HODL",
                        "M00NB0115",
                        "T1337",
                        start,
                        end
                ))

                val start2 = start.clone() as Calendar
                start2.add(Calendar.HOUR_OF_DAY, 12)
                val end2 = end.clone() as Calendar
                end2.add(Calendar.HOUR_OF_DAY, 14)
                schedule.add(Lesson(
                        "OVERNIGHT",
                        "testing123",
                        "somewhere over the rainbow",
                        start2,
                        end2
                ))
            }

        } else {
            TODO("i HaVe CrIpPlInG dEpReSsIoN")
        }

        return schedule
    }
}