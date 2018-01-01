package com.spcore.helpers

import com.alamkanak.weekview.WeekViewEvent
import com.spcore.models.Event
import com.spcore.models.User
import java.util.*

/*
    HARDCODE_MODE

    Hardcoded Users:
 */
val NatoshiSakamoto = User("1111111", "natoshi_sakamoto", "Natoshi Sakamoto", arrayListOf())
val NatoshiSakamoto1 = User("1111112", "natoshi_sakamoto1", "Natoshi Sakamoto", arrayListOf())
val NatoshiSakamoto2 = User("1111113", "natoshi_sakamoto2", "Natoshi Sakamoto", arrayListOf())
val NatoshiSakamoto3 = User("1111114", "natoshi_sakamoto3", "Natoshi Sakamoto", arrayListOf())

fun natoshiSchedule(year: Int, month: Int): ArrayList<WeekViewEvent> {
    val schedule = ArrayList<WeekViewEvent>()

    val cal = newCalendar(year, month, 1)

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
                NatoshiSakamoto,
                deletedInvite = arrayListOf(DerpMcDerpson)
        ))
    }

    return schedule
}

val DerpMcDerpson = User("2222222", "derpmcderpson", "Derp McDerpson", arrayListOf())

fun derpSchedule(year: Int, month: Int): ArrayList<WeekViewEvent> {
    val schedule = ArrayList<WeekViewEvent>()

    val cal = newCalendar(year, month, 1)

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
                DerpMcDerpson,
                deletedInvite = arrayListOf(NatoshiSakamoto)
        ))
    }

    return schedule
}

val SimonFransman = User("3333333", "yalikejazz", "Simon Fransman", arrayListOf())

val AdamNeely = User("4444444", "thelick", "BASS", arrayListOf())

/*
    HARDCODE_MODE

    Hardcoded events
 */

val HardcodedEvents = mutableListOf(Event(
        "Pre-SIP Lunch",
        "Hanging out with mah lads before the big SIPPP",
        "J Cube",
        newCalendar(2018, 0, 15, 13),
        newCalendar(2018, 0, 15, 15, 0, 0),
        Auth.user,
        going = arrayListOf(NatoshiSakamoto,NatoshiSakamoto1,NatoshiSakamoto2,NatoshiSakamoto3),
        notGoing = arrayListOf(DerpMcDerpson),
        haventReplied = arrayListOf(AdamNeely),
        deletedInvite = arrayListOf(SimonFransman),
        id = 0
))