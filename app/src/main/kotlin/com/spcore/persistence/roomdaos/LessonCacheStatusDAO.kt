package com.spcore.persistence.roomdaos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.spcore.persistence.LessonCacheStatus

@Dao
interface LessonCacheStatusDAO {
    @Query("SELECT * FROM lessoncachestatus")
    fun getCachedMonths(): List<LessonCacheStatus>

    @Query("DELETE FROM lessoncachestatus")
    fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun markMonthAsCached(lessonCacheStatus: LessonCacheStatus)
}