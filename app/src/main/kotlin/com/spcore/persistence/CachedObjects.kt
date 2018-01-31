package com.spcore.persistence

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.spcore.helpers.toCalendar
import com.spcore.models.Lesson

@Entity(tableName = "lessons")
data class CachedLesson(
        @PrimaryKey(autoGenerate = false)
        val base16ID: String,
        val moduleCode: String,
        val moduleName: String,
        val lessonType: String,
        val location: String,
        val startTime: Long,
        val endTime: Long
) {
        fun toLesson() =
                Lesson(moduleName, moduleCode, location, lessonType, startTime.toCalendar(), endTime.toCalendar(), base16ID)
}

// This table stores months that has already been cached in YYYYMM format
@Entity(tableName = "lessoncachestatus")
data class LessonCacheStatus(
        @PrimaryKey(autoGenerate = false)
        val YYYYMM: String
) {
        constructor(year: Int, month: Int) :
                this("%04d%02d".format(year, month))

        override fun equals(other: Any?): Boolean {
                return other is LessonCacheStatus && other.YYYYMM == this.YYYYMM
        }
}