package com.spcore.spmobileapi

/**
 * Models contained here are translations of the raw JSONModels into a more optimised format.
 * These are the data types that will be utilised and exposed outside of the library
 */

import com.spcore.spmobileapi.api.ServerResponseException
import com.spcore.spmobileapi.api.TimetableDayResponse
import com.spcore.spmobileapi.helpers.Strings
import com.spcore.spmobileapi.helpers.reduceToString
import com.spcore.spmobileapi.helpers.toWords
import com.spcore.spmobileapi.psuedomodels.Time
import okhttp3.ResponseBody
import java.sql.Timestamp
import java.time.LocalTime
import java.util.*


sealed class Result {
    class NoInternet(val message: String = "") : Result()
    class ConnectionError(val errorBody: ResponseBody?) : Result()
    class InvalidArguments(val message: String = "") : Result()
    class UnexpectedError(val message: UnexpectedErrorType) : Result()

    enum class UnexpectedErrorType {
        NO_RESPONSE_BODY
    }
}

class Timetable : Result() {

    init {

    }


}

class Day
internal constructor(rawDayTimetable: TimetableDayResponse, ddmmyy: String)
    : Result() {

    val dayOfWeek: DayOfWeek
    val lessons: List<Lesson>

    init {
        if (rawDayTimetable.responseType != TimetableDayResponse.ResponseType.VALID)
            throw ServerResponseException("Abnormal timetable day response: ${rawDayTimetable.responseType}")

        val cal = Calendar.getInstance()
        cal.time = ddmmyyFormat.parse(ddmmyy) // Beware internal date parse exception error
        dayOfWeek = DayOfWeek.fromOrdinal(cal.get(Calendar.DAY_OF_WEEK))

        lessons = rawDayTimetable.timetable
                .map { Lesson(it.module) }
                .sortedBy { it.startTime }
    }

    override fun toString(): String {
        return "$dayOfWeek:\t${lessons.reduceToString { a, x -> "$a\n\t$x"}}"
    }

    class Lesson(val abbr:          String,
                 val code:          String,
                 val location:      String,
                 val type:          String,
                 timeStr:           String) {

        val startTime: Time
        val endTime: Time

        internal constructor(m: TimetableDayResponse.TimetableModuleObject.ModuleData) :
                this(m.abbr, m.code, m.location, m.type, m.time)

        init {

            startTime = Time(timeStr.substring(0..4))
            endTime = Time(timeStr.substring(6..10))
        }

        override fun toString(): String {
            return "$abbr ($code): @$location  $startTime ~ $endTime  [$type]"
        }

    }

    enum class DayOfWeek(val ord: Int) {
        SUNDAY(1), MONDAY(2), TUESDAY(3),
        WEDNESDAY(4), THURSDAY(5), FRIDAY(6),
        SATURDAY(7);

        companion object {
            private val valuesCache = DayOfWeek.values()

            fun fromOrdinal(ord: Int) : DayOfWeek {
                if (ord !in 1..7)
                    throw IllegalArgumentException("Day of week can only be from 1 to 7. How did this even happen?")

                return valuesCache[ord - 1]
            }
        }

        override fun toString(): String {
            return this.name.toWords(Strings.WordDelimiterType.UNDERSCORE_DELIMITED).render()
        }
    }
}