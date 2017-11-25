package com.spcore.models

import com.alamkanak.weekview.WeekViewEvent
import com.spcore.R
import java.util.*

abstract class ScheduleItem {
    abstract fun toWeekViewEvent() : WeekViewEvent
}

class Lesson(val moduleName: String,
             val moduleCode: String,
             val location: String,
             val start: Calendar,
             val end: Calendar,
             val id: Int = 1 //Objects.hash(moduleCode, start, end)
) : ScheduleItem() {

    override fun toWeekViewEvent(): WeekViewEvent {
        val e = WeekViewEvent(id.toLong(), moduleName, location, start, end)
        e.color = R.color.neutralLesson
        return e
    }

}