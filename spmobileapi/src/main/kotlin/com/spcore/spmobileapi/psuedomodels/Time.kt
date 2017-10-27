package com.spcore.spmobileapi.psuedomodels

import com.spcore.spmobileapi.hhmm
import java.text.ParseException
import java.util.*

class Time(var date: Date) : Comparable<Time> {
    constructor(timeStr: String) : this(hhmm.parse(timeStr))

    var time
        get() = try { hhmm.format(date) } catch (e: ParseException) { "Invalid date" }
        set(timeStr) {
            date = hhmm.parse(timeStr)
            timeStr
        }

    override fun compareTo(other: Time): Int = date.compareTo(other.date)
}