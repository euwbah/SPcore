package com.spcore.apis

import com.spcore.helpers.toCalendar
import com.spcore.models.Lesson

// class LoginResponse(val token: String, val isInitial: Boolean)
class LoginResponse(val token: String, val username: String?, val displayName: String?)

class StringResponse(val response: String)

class LessonResponse(
        val id: String,
        val moduleName: String,
        val moduleCode: String,
        val lessonType: String,
        val location: String,
        val startTime: Long,
        val endTime: Long) {
    fun toLesson() =
            Lesson(moduleName, moduleCode, location, lessonType, startTime.toCalendar(), endTime.toCalendar(), id)
}