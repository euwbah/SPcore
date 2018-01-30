package com.spcore.persistence

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.spcore.persistence.roomdaos.LessonCacheStatusDAO
import com.spcore.persistence.roomdaos.LessonDAO

@Database(entities = arrayOf(CachedLesson::class, LessonCacheStatus::class), version = 1)
abstract class _SPCoreLocalDB : RoomDatabase() {
    abstract fun lessonDAO(): LessonDAO
    abstract fun lessonCacheStatusDAO(): LessonCacheStatusDAO
}

fun initLocalDB(context: Context) {
    fSPCoreLocalDB = Room.databaseBuilder(context, _SPCoreLocalDB::class.java, "spcore-local-db").build()
}

private var fSPCoreLocalDB: _SPCoreLocalDB? = null

/**
 * REMEMBER TO CALL INIT WITH [initLocalDB] FIRST IF NOT [NullPointerException] WILL BE THROWN!
 */
val SPCoreLocalDB: _SPCoreLocalDB
    get() = fSPCoreLocalDB!!