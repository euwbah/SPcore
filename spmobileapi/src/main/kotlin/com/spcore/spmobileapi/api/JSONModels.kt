/**
 * JSONModels mimic the unaltered format of the JSON data returned from the SP API endpoint.
 * This is only to be used internally within the API wrapper
 */
package com.spcore.spmobileapi.api

import com.spcore.spmobileapi.api.TimetableDayResponse.ResponseType.*
import com.spcore.spmobileapi.UTCFormat
import java.util.*

internal class TimetableDayResponse(val timetable: List<TimetableModuleObject>) {

    /** This prop. will be used later during the translation process to determine validity of the
     *  response.
     */
    var status: ResponseType? = null // remember init value is written to the backing field
        get() = field ?: let {
                    timetable.forEach {
                        when(it.module.validity) {
                            STUD_ID_NONEXISTENT -> return STUD_ID_NONEXISTENT
                            TIMETABLE_NOT_AVAILABLE_YET -> return TIMETABLE_NOT_AVAILABLE_YET
                            NO_LESSONS -> return NO_LESSONS
                        }
                    }

                    field = NORMAL

                    field
                }
        private set(v) {field = v}


    enum class ResponseType {
        NORMAL,

        /**
         * i.e. during the holidays
         */
        TIMETABLE_NOT_AVAILABLE_YET,

        /**
         * i.e. saturdays/sundays
         */
        NO_LESSONS,

        /**
         * This should really never happen, as the app will require auth even before it can be used
         */
        STUD_ID_NONEXISTENT
    }

    class TimetableModuleObject(val module: ModuleData) {

        class ModuleData(val abbr: String,      // "DSAL"
                         val time: String,      // "0800-1000"
                         val location: String,  // "T2013"
                         val type: String,      // "LECT"
                         val code: String) {    // "ST1337"
            var validity: ResponseType? = null
                get() = field ?: let {
                    field = when(abbr) {
                        "Stud id not found"                 -> STUD_ID_NONEXISTENT
                        "Time Table is not available yet"   -> TIMETABLE_NOT_AVAILABLE_YET
                        ""                                  -> NO_LESSONS

                        // FIXME HACK XXX XXX XXX XXX XXX XXX XXX XXX XXX XXX XXX XXX XXX XXX XX XX
                        // This is just a wild guess... there may be other errors the server may
                        // return that are not included above
                        else                                -> NORMAL
                    }

                    field
                }
        }

        override fun toString(): String {
            return "${module.abbr}>\n" +
                    "    time: ${module.time}" +
                    "     loc: ${module.location}" +
                    "    type: ${module.type}" +
                    "    code: ${module.code}"
        }
    }

    override fun toString(): String {
        return if (status == NORMAL)
            timetable
                .map    { it.toString() }
                .reduce { acc, s -> "$acc\n\n$s" }
        else
            "ERROR: Student ID doesn't exist"
    }

}

internal class CalendarResponse(val items: List<CalendarItem>) {
    class CalendarItem(val summary: String,
                       val colorId: String,     // This attr can also be used to determine type of calendar period
                       val start: DateObject,
                       val end: DateObject) {

        class DateObject(private val dateTimeStr: String) {
            @Transient var dateTime: Date? = null
                get() = field ?: let {
                    field = UTCFormat.parse(dateTimeStr)
                    field
                }
        }

        override fun toString(): String {
            var x = 0
            return "$summary:\n" +
                    "     type: $colorId\n" +
                    "    start: ${start.dateTime.toString()}\n" +
                    "      end: ${end.dateTime.toString()}"
        }
    }

    override fun toString(): String {
        return items
                .map {it.toString()}
                .reduce{a, b -> "$a\n\n$b"}
    }
}