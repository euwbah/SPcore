package com.spcore.helpers

import com.alamkanak.weekview.WeekViewEvent
import com.spcore.models.Event
import com.spcore.models.Lesson
import com.spcore.models.User
import java.util.*
import kotlin.collections.ArrayList


object HardcodedStuff {
    /*
        HARDCODE_MODE

        Hardcoded Users:
     */
    val NatoshiSakamoto = User("1111111", "natoshi_sakamoto", "Natoshi Sakamoto")
    val NatoshiSakamoto1 = User("1111112", "natoshi_sakamoto1", "Natoshi Sakamoto")
    val NatoshiSakamoto2 = User("1111113", "natoshi_sakamoto2", "Natoshi Sakamoto")
    val NatoshiSakamoto3 = User("1111114", "natoshi_sakamoto3", "Natoshi Sakamoto")

    val WatermelonMan = User("1111115", "watermel0n", "Watermelon Man")

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

    val DerpMcDerpson = User("2222222", "derpmcderpson", "Derp McDerpson")

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

    val SimonFransman = User("3333333", "yalikejazz", "Simon Fransman")

    val AdamNeely = User("4444444", "thelick", "BASS")

    val HardcodedUsers = arrayListOf(
            NatoshiSakamoto, NatoshiSakamoto1, NatoshiSakamoto2, NatoshiSakamoto3,
            DerpMcDerpson, SimonFransman, AdamNeely, WatermelonMan)

    val HardcodedFriends = arrayListOf(
            NatoshiSakamoto, NatoshiSakamoto1, NatoshiSakamoto2,
            DerpMcDerpson, SimonFransman, WatermelonMan)


/*
   Hardcoded lessons
 */

    /**
     * Only used for [HARDCODE_MODE]
     */
    class WeeklyLesson(val moduleName: String, val moduleCode: String, val location: String, val lessonType: String,
                       val startTime: Duration, val endTime: Duration)

    val HardcodedLessons = mapOf(
            Calendar.MONDAY to listOf(
                    WeeklyLesson("DEUI", "ST0277", "T2253", "LAB",
                            Duration(hours=11, minutes = 30),
                            Duration(hours=13, minutes = 30))
            ),
            Calendar.TUESDAY to listOf(
                    WeeklyLesson("NETS", "ST2501", "T2036", "LAB",
                            Duration(hours=8, minutes = 0),
                            Duration(hours=11, minutes = 0)),
                    WeeklyLesson("MAPP", "ST0281", "T2253", "TUT",
                            Duration(hours=14, minutes = 0),
                            Duration(hours=16, minutes = 0)),
                    WeeklyLesson("SEP", "ST293Z", "T2142", "TUT",
                            Duration(hours=16, minutes = 0),
                            Duration(hours=18, minutes = 0))
            ),
            Calendar.WEDNESDAY to listOf(
                    WeeklyLesson("ENBP", "ST2219", "T2257", "TUT",
                            Duration(hours=10, minutes = 0),
                            Duration(hours=12, minutes = 0)),
                    WeeklyLesson("NETS", "ST2501", "T2251", "LEC",
                            Duration(hours=13, minutes = 0),
                            Duration(hours=14, minutes = 30))
            ),
            Calendar.THURSDAY to listOf(
                    WeeklyLesson("MAPP", "ST0281", "T2031", "LAB",
                            Duration(hours=8, minutes = 0),
                            Duration(hours=11, minutes = 0)),
                    WeeklyLesson("ENBP", "ST2219", "T2031", "LAB",
                            Duration(hours=11, minutes = 0),
                            Duration(hours=13, minutes = 30)),
                    WeeklyLesson("SIP", "LC8003", "T1635", "TUT",
                            Duration(hours=15, minutes = 0),
                            Duration(hours=17, minutes = 0))
            ),
            Calendar.FRIDAY to listOf(
                    WeeklyLesson("NETS", "ST2501", "T2012", "TUT",
                            Duration(hours=8, minutes = 0),
                            Duration(hours=9, minutes = 0)),
                    WeeklyLesson("SEP", "ST293Z", "T2142", "LAB",
                            Duration(hours=9, minutes = 0),
                            Duration(hours=12, minutes = 0)),
                    WeeklyLesson("DEUI", "ST0277", "T2012", "TUT",
                            Duration(hours=13, minutes = 0),
                            Duration(hours=15, minutes = 0))

            )
    )

/*
    HARDCODE_MODE

    Hardcoded events
 */

    val HardcodedEvents = mutableListOf<Event>()


    fun initialize(currUser: User) {
        HardcodedEvents.add(Event(
                "Pre-SIP Lunch",
                "Hanging out with mah lads before the big SIPPP",
                "J Cube",
                newCalendar(2018, 0, 15, 11),
                newCalendar(2018, 0, 15, 13, 0, 0),
                NatoshiSakamoto,
                going = arrayListOf(NatoshiSakamoto,NatoshiSakamoto1, NatoshiSakamoto2, NatoshiSakamoto3),
                notGoing = arrayListOf(DerpMcDerpson),
                haventReplied = arrayListOf(Auth.user),
                deletedInvite = arrayListOf(SimonFransman),
                id = 0
        ))

        HardcodedEvents.add(Event(
                "Post-SIP Lunch",
                "Eat again i guess?",
                "Marina Bay Sands",
                newCalendar(2018, 0, 15, 15),
                newCalendar(2018, 0, 15, 17, 0, 0),
                NatoshiSakamoto,
                going = arrayListOf(NatoshiSakamoto,NatoshiSakamoto1, NatoshiSakamoto2, NatoshiSakamoto3),
                notGoing = arrayListOf(DerpMcDerpson),
                haventReplied = arrayListOf(Auth.user),
                deletedInvite = arrayListOf(SimonFransman),
                id = 2
        ))

        HardcodedEvents.add(Event(
                "Smoke trees",
                "Dinner round 1",
                "Changi Village",
                newCalendar(2018, 0, 15, 17),
                newCalendar(2018, 0, 15, 18, 0, 0),
                NatoshiSakamoto,
                going = arrayListOf(DerpMcDerpson, WatermelonMan),
                notGoing = arrayListOf(NatoshiSakamoto3),
                haventReplied = arrayListOf(Auth.user),
                id = 3

        ))

        HardcodedEvents.add(Event(
                "Camping at Ubin",
                "Idk let's get some sun I guess?",
                "Pulau Ubin",
                newCalendar(2018, 0, 15, 18),
                newCalendar(2018, 0, 16, 6, 0, 0),
                NatoshiSakamoto,
                going = arrayListOf(NatoshiSakamoto2),
                haventReplied = arrayListOf(DerpMcDerpson, WatermelonMan,Auth.user),
                id = 4
        ))

        HardcodedEvents.add(Event(
                "Eat at Fc5",
                "Lunch at FC5",
                "fc5",
                newCalendar(2018, 0, 4, 13),
                newCalendar(2018, 0, 4, 14, 0, 0),
                NatoshiSakamoto,
                going = arrayListOf(NatoshiSakamoto, Auth.user),
                haventReplied = arrayListOf(DerpMcDerpson, WatermelonMan),
                id = 5
        ))

        HardcodedEvents.add(Event(
                "Smoke more trees",
                "BiG shAQ",
                "Changi Village",
                newCalendar(2018, 0, 16, 18),
                newCalendar(2018, 0, 16, 20, 0, 0),
                NatoshiSakamoto,
                going = arrayListOf(DerpMcDerpson, WatermelonMan),
                notGoing = arrayListOf(NatoshiSakamoto3),
                haventReplied = arrayListOf(Auth.user),
                id = 6
        ))
    }
}
