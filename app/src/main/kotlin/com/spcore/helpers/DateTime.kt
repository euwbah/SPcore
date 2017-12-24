package com.spcore.helpers

import java.util.*
import com.spcore.helpers.*

fun Date.toCalendar(): Calendar {
    val cal = Calendar.getInstance()
    cal.time = this
    return cal
}

fun Calendar.startOfDay(): Calendar {
    val cal = this.clone() as Calendar
    cal.set(Calendar.MILLISECOND, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.HOUR_OF_DAY, 0)

    return cal
}

fun Calendar.isYesterday(): Boolean {
    return this.startOfDay() + Duration(days=1) == Calendar.getInstance().startOfDay()
}
fun Calendar.isToday(): Boolean {
    return this.startOfDay() == Calendar.getInstance().startOfDay()
}
fun Calendar.isTomorrow(): Boolean {
    return this.startOfDay() - Duration(days=1) == Calendar.getInstance().startOfDay()
}

fun humanReadableTimeRange(time1: Calendar, time2: Calendar) : String {
    val now = Calendar.getInstance()

    // The general idea here is that if a date component can be inferred,
    // don't display it

    // If this year is the same as time1's year and time2's year, no need to show
    // year because it is implied that the current year is year
    val showSecondYear = time1.get(Calendar.YEAR) != time2.get(Calendar.YEAR)
    val showFirstYear = showSecondYear || now.get(Calendar.YEAR) != time1.get(Calendar.YEAR)

    val firstYearString = if(showFirstYear) " " + time1.get(Calendar.YEAR).toString() else ""
    val secondYearString = if(showSecondYear) " " + time2.get(Calendar.YEAR).toString() else ""

    val showSecondDate = time1.startOfDay() != time2.startOfDay()

    val firstDateStr =
            if(time1.isYesterday()) "Yesterday"
            else if(time1.isToday()) "Today"
            else if(time1.isTomorrow()) "Tomorrow"
            else
                time1.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) + " " +
                time1.get(Calendar.DAY_OF_MONTH).toString() + " " +
                time1.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())

    val secondDateStr =
            if(!showSecondDate) ""
            else if(time2.isYesterday()) "Yesterday"
            else if(time2.isToday()) "Today"
            else if(time2.isTomorrow()) "Tomorrow"
            else
                time2.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) + " " +
                time2.get(Calendar.DAY_OF_MONTH).toString() + " " +
                time2.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())

    val firstTimeStr =
            time1.get(Calendar.HOUR).let {
                if(it == 0)
                    12
                else
                    it
            }.toString() +
            time1.get(Calendar.MINUTE).let {
                if(it == 0)
                    ""
                else
                    ":%02d".format(it)
            } +
            " " +
            if(time1.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"

    val secondTimeStr =
            time2.get(Calendar.HOUR).let {
                if(it == 0)
                    12
                else
                    it
            }.toString() +
                    time2.get(Calendar.MINUTE).let {
                        if(it == 0)
                            ""
                        else
                            ":%02d".format(it)
                    } +
                    " " +
                    if(time2.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"

    return if(showSecondDate) {
        "$firstDateStr$firstYearString, $firstTimeStr -\n" +
        "$secondDateStr$secondYearString, $secondTimeStr"
    } else {
        "$firstDateStr$firstYearString\n" +
        "$firstTimeStr - $secondTimeStr"
    }
}

/**
 * Infix method chaining builder for checking whether a Calendar value is
 * between some start and end time
 *
 * @param cal The time in question
 * @param start the start time
 * @param startExclusive whether or not the start bound is inclusivve or exclusive
 */
class CalendarRangeBuilder internal constructor(val cal: Calendar, val start: Calendar, val startInclusive: Boolean) {
    /**
     * Inclusive upper bounds
     *
     * @see upTo
     */
    infix fun to(end: Calendar): Boolean {
        return !cal.after(end) &&   if(startInclusive)
                                        !cal.before(start)
                                    else
                                        cal.after(start)
    }

    /**
     * Exclusive upper bounds
     *
     * @see to
     */
    infix fun upTo(end: Calendar): Boolean {
        return cal.before(end) &&   if(startInclusive)
                                        !cal.before(start)
                                    else
                                        cal.after(start)
    }
}

/**
 * Exclusive lower bounds
 *
 * @see isFrom
 */
infix fun Calendar.isBetween(start: Calendar): CalendarRangeBuilder {
    return CalendarRangeBuilder(this, start, false)
}

/**
 * Inclusive lower bounds
 *
 * @see isBetween
 */
infix fun Calendar.isFrom(start: Calendar): CalendarRangeBuilder {
    return CalendarRangeBuilder(this, start, true)

}

class Duration(
        val days: Int = 0,
        val hours: Int = 0,
        val minutes: Int = 0,
        val seconds: Int = 0,
        val millis: Int = 0
) {
    fun toMillis(): Int {
        return  millis +
                seconds * 1000 +
                minutes * 1000 * 60 +
                hours   * 1000 * 60 * 60 +
                days    * 1000 * 60 * 60 * 24
    }
}

/**
 * Provides a way to do basic addition of a timespan to a Calendar object
 * without killing it
 */
operator fun Calendar.plus(duration: Duration): Calendar {
    val newCalendar = this.clone() as Calendar
    newCalendar.add(Calendar.MILLISECOND, duration.toMillis())
    return newCalendar
}

/**
 * Provides a way to do basic addition of a timespan to a Calendar object
 * without killing it
 */
operator fun Calendar.minus(duration: Duration): Calendar {
    val newCalendar = this.clone() as Calendar
    newCalendar.add(Calendar.MILLISECOND, -duration.toMillis())
    return newCalendar
}