package com.spcore.persistence

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "lessons")
data class CachedLesson(
        @PrimaryKey(autoGenerate = false)
        val base24ID: String,
        val moduleCode: String,
        val moduleName: String,
        val lessonType: String,
        val location: String,
        val startTime: Long,
        val endTime: Long
)