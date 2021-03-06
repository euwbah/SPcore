package com.spcore.spmobileapi

/**
 * Models contained here are translations of the raw JSONModels into a more optimised format.
 * These are the data types that will be utilised and exposed outside of the library
 */

import android.os.Parcel
import android.os.Parcelable
import com.spcore.spmobileapi.api.TimetableDayResponse
import com.spcore.spmobileapi.helpers.Strings
import com.spcore.spmobileapi.helpers.reduceToString
import com.spcore.spmobileapi.helpers.toWords
import com.spcore.spmobileapi.psuedomodels.Time
import java.io.ObjectInputStream
import java.io.Serializable
import java.util.*

sealed class Result<out ValueType, out ErrorType> {
    class Ok<out ValueType, out ErrorType>(val okValue: ValueType) : Result<ValueType, ErrorType>()

    class Error<out ValueType, out ErrorType>(val errorValue: ErrorType) : Result<ValueType, ErrorType>()

    inline fun tryGetIfNot(handle: (ErrorType) -> Unit): ValueType? {
        return when(this) {
            is Ok -> okValue
            is Error -> {
                handle(errorValue)
                null
            }
        }
    }
}

interface CanErr<ErrorType> {
    val error: ErrorType?
}

class Timetable {
    init {

    }
}

/**
 * Note that a day without lessons (i.e. `lessons = null`/`lessons.size() = 0`) is
 * to be taken literally as a day without lessons (i.e. saturday/sunday), and does
 * not constitute as an error.
 *
 * A mechanism separate from the `Result.Error` wrapper would probably have to handle
 * the UI to inform the user in the above case
 */
class Day
internal constructor(rawDayTimetable: TimetableDayResponse, ddmmyy: String): CanErr<Day.Errors> {

    val dayOfWeek: DayOfWeek
    val lessons: List<Lesson>?
    override val error: Errors?

    init {

        val cal = Calendar.getInstance()
        cal.time = ddmmyyFormat.parse(ddmmyy) // Beware internal date parse exception error
        dayOfWeek = DayOfWeek.fromOrdinal(cal.get(Calendar.DAY_OF_WEEK))


        error = when (rawDayTimetable.status) {
            TimetableDayResponse.ResponseType.TIMETABLE_NOT_AVAILABLE_YET ->
                Errors.TIMETABLE_NOT_AVAILABLE_YET
            TimetableDayResponse.ResponseType.STUD_ID_NONEXISTENT ->
                Errors.NO_SUCH_STUD_ID
            else -> null
        }

        lessons =
            if (rawDayTimetable.status != TimetableDayResponse.ResponseType.NORMAL)
                null
            else
                rawDayTimetable.timetable
                .map { Lesson(it.module) }
                .sortedBy { it.startTime }
    }

    companion object {
        internal fun wrapAsResult(foo: () -> Day): Result<Day, Errors> {
            val x = foo()
            return x.error?.let {
                Result.Error<Day, Day.Errors>(it)
            } ?:
                Result.Ok(x)
        }
    }

    enum class Errors {
        TIMETABLE_NOT_AVAILABLE_YET,
        NO_SUCH_STUD_ID
    }

    override fun toString(): String {
        return "$dayOfWeek:\t${lessons?.reduceToString { a, x -> "$a\n\t$x"} ?: "No lessons"}"
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

class ATSResult(override val error: Errors?) : CanErr<ATSResult.Errors> {
    companion object {
        /**
         * Return [Errors] from [foo] and it will construct a `Result.Error`.
         * Returning any other value type will result in a valueless `Result.Ok`
         */
        internal fun <T> wrapAsResult(foo: () -> T?): Result<Nothing?, Errors> {
            val x = foo()
            return when(x) {
                is Errors ->
                    Result.Error(x)
                else ->
                    Result.Ok(null)
            }
        }
    }
    sealed class Errors {
        object INVALID_CODE : Errors()
        object ALREADY_ENTERED : Errors()

        /**
         * @param wrongClass The class of which ATS code the user mistook for his own
         */
        class WRONG_CLASS(val wrongClass: String) : Errors()

        object NO_INTERNET : Errors()
        object NOT_CONNECTED_TO_SCHOOL_WIFI : Errors()
        object INVALID_CREDENTIALS : Errors()

        /**
         * Converts the error monad into a human-readable error message
         */
        override fun toString() : String {
            return when(this) {
                ATSResult.Errors.INVALID_CODE -> "Invalid ATS code was entered, enter again."
                ATSResult.Errors.ALREADY_ENTERED -> "ATS code was already submitted"
                is ATSResult.Errors.WRONG_CLASS -> {
                    val `class` = this.wrongClass
                    "ATS code was for class $`class`"
                }
                ATSResult.Errors.NO_INTERNET -> "No internet connection"
                ATSResult.Errors.NOT_CONNECTED_TO_SCHOOL_WIFI -> "Not connected to SPStudent WiFi"
                ATSResult.Errors.INVALID_CREDENTIALS -> "SPice account password was just changed, log out and log in again"
            }
        }

        fun toSerializable() : _Serializable {
            return when(this) {
                ATSResult.Errors.INVALID_CODE -> _Serializable("invalid code")
                ATSResult.Errors.ALREADY_ENTERED -> _Serializable("already entered")
                is ATSResult.Errors.WRONG_CLASS -> _Serializable("wrong class", this.wrongClass)
                ATSResult.Errors.NO_INTERNET -> _Serializable("no internet")
                ATSResult.Errors.NOT_CONNECTED_TO_SCHOOL_WIFI -> _Serializable("not connected to school wifi")
                ATSResult.Errors.INVALID_CREDENTIALS -> _Serializable("invalid credentials")
            }
        }

        class _Serializable
            internal constructor(val type: String, val param1: String = "") : Serializable {

            fun deserialize(): Errors {
                return when(type) {
                    "invalid code" -> INVALID_CODE
                    "already entered" -> ALREADY_ENTERED
                    "wrong class" -> WRONG_CLASS(param1)
                    "no internet" -> NO_INTERNET
                    "not connected to school wifi" -> NOT_CONNECTED_TO_SCHOOL_WIFI
                    "invalid credentials" -> INVALID_CREDENTIALS
                    else -> throw ABall()
                }
            }
        }
    }
}