package com.spcore.persistence.roomdaos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.spcore.models.Lesson
import com.spcore.persistence.CachedLesson

@Dao
interface LessonDAO {
    @Query("SELECT * FROM lessons")
    fun getCachedLessons(): List<CachedLesson>

    @Query("DELETE FROM lessons")
    fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLesson(lesson: CachedLesson)

    fun insertLesson(lesson: Lesson) {
        lesson.apply {
            insertLesson(CachedLesson(
                base24ID,
                moduleCode,
                name,
                lessonType,
                location,
                startTime.timeInMillis,
                endTime.timeInMillis
            ))
        }
    }
}