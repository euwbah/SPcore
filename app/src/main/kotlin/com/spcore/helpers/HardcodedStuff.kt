package com.spcore.helpers

import com.alamkanak.weekview.WeekViewEvent
import com.spcore.models.Event
import com.spcore.models.User
import java.util.*

val NatoshiSakamoto = User("1111111", "natoshi_sakamoto", "Natoshi Sakamoto", arrayListOf())

fun natoshiSchedule(year: Int, month: Int): ArrayList<WeekViewEvent> {
    val schedule = ArrayList<WeekViewEvent>()

    val cal = Calendar.getInstance().apply { clear(); set(year, month, 1) }

    val lastDate = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    for(i in 0 until lastDate) {
        val start = (cal.clone() as Calendar) + Duration(days = i, hours = 9)
        val end = (start.clone() as Calendar) + Duration(hours = 8)

        schedule.add(Event(
                "Make Bitcoin Great Again",
                "When s2x?",
                "David Jones' Server Room",
                start,
                end,
                deletedInvite = arrayListOf(DerpMcDerpson)
        ))
    }

    return schedule
}

val DerpMcDerpson = User("2222222", "derpmcderpson", "Derp McDerpson", arrayListOf())

fun derpSchedule(year: Int, month: Int): ArrayList<WeekViewEvent> {
    val schedule = ArrayList<WeekViewEvent>()

    val cal = Calendar.getInstance().apply { clear(); set(year, month, 1) }

    val lastDate = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    for(i in 0 until lastDate) {
        val start = (cal.clone() as Calendar) + Duration(days = i, hours = 9)
        val end = (start.clone() as Calendar) + Duration(hours = 8)

        schedule.add(Event(
                "The daily meme",
                "Mimis 4 lyfe",
                "Some house's basement",
                start,
                end,
                deletedInvite = arrayListOf(NatoshiSakamoto)
        ))
    }

    return schedule
}