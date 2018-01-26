package com.spcore.apis

// class LoginResponse(val token: String, val isInitial: Boolean)
class LoginResponse(val token: String, val username: String?, val displayName: String?)

class StringResponse(val response: String)

class LessonResponse(
        val id: String,
        val moduleCode: String,
        val moduleName: String,
        val lessonType: String,
        val location: String,
        val startTime: Long,
        val endTime: Long)