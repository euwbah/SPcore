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

    /**
     * Clears all lessons of which [CachedLesson.startTime] falls between the [startTimestamp] inclusively
     * and the [endTimestamp] exclusively
     */
    @Query("DELETE FROM lessons WHERE startTime >= :startTimestamp AND startTime < :endTimestamp")
    fun clear(startTimestamp: Long, endTimestamp: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLesson(lesson: CachedLesson)
}