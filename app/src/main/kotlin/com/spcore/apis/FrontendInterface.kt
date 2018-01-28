package com.spcore.apis

import android.util.Log
import com.alamkanak.weekview.WeekViewEvent
import com.google.firebase.iid.FirebaseInstanceId
import com.spcore.activities.InitialLoginActivity
import com.spcore.activities.LoginActivity.LoginStatus
import com.spcore.helpers.*
import com.spcore.models.Event
import com.spcore.models.Lesson
import com.spcore.helpers.HardcodedStuff.HardcodedEvents
import com.spcore.helpers.HardcodedStuff.HardcodedLessons
import com.spcore.persistence.CachedLesson
import com.spcore.persistence.SPCoreLocalDB
import java.util.*
import kotlin.collections.ArrayList

/**
 * A collection of synchronous methods which convert raw data from backend into
 * however the frontend needs to implement it.
 *
 * These functions abstract out HARDCODE_MODE from implementation so
 * the view controllers will not need to do so
 *
 * REMEMBER TO CALL THESE FROM A BACKGROUND THREAD IF NOT THE APP WILL CLOSE
 */
object FrontendInterface {
    /**
     * NOTE: This will also update the FirebaseInstanceId Firebase registration token
     * on the server-side
     */
    fun performLogin(adminNo: String, password: String) : LoginStatus {
//        if (HARDCODE_MODE) {
//            // Simulate server access
//            Thread.sleep(500)
//            val fakeResponse = LoginResponse("Fake JWT Token")
//            return when (adminNo) {
//                "1234567" -> LoginStatus.SUCCESS(fakeResponse)
//                "7654321" -> LoginStatus.SP_SERVER_DOWN
//                else -> LoginStatus.INVALID_CREDENTIALS
//            }
//        }

        val resp = Backend.performLogin(adminNo, password, FirebaseInstanceId.getInstance().token).execute()

        if(!resp.isSuccessful) {
            resp.errorBody()?.string()?.let {
                if (it.isBlank())
                    return@let

                val err = backendErrorAdapter.fromJson(it)

                err?.code?.let {
                    return when(it) {
                        Backend.WRONG_SPICE_CRENDENTIALS -> LoginStatus.INVALID_CREDENTIALS
                        Backend.DATABASE_ERROR -> LoginStatus.SP_SERVER_DOWN
                        Backend.LOCKED_OUT_BY_SP -> LoginStatus.LOCKED_OUT_BY_SP
                        else -> LoginStatus.UNEXPECTED_ERROR(it, err.msg)
                    }
                }
            }

            return LoginStatus.VOID
        }

        resp.body()?.let {
            if (it.username != null) {
                Auth.setUserInitializedLocally(it.username, it.displayName)
            }
            return LoginStatus.SUCCESS(it)
        }

        return LoginStatus.VOID
    }

    // NOTE: month is 1-based
    fun getSchedule(adminNo: String, year: Int, month: Int) : List<WeekViewEvent> {
        val schedule = ArrayList<WeekViewEvent>()

        if (CacheState.checkNeedToRefreshLessonsCache()) {
            val resp =
                    Backend.getLessons("%04d%02d".format(year, month)).execute()

            if (!resp.isSuccessful) {
                // This shouldn't fall through since the server doesn't return any errors in this endpoint
                resp.errorBody()?.string()?.let {
                    if (it.isBlank())
                        return@let

                    val err = backendErrorAdapter.fromJson(it)

                    Log.w("SPCore", "Backend Interface error: unable to get lessons. ${err?.msg}")
                }

                return schedule
            }

            SPCoreLocalDB.lessonDAO().clear(
                    newCalendar(year, month - 1, 1).timeInMillis,
                    (newCalendar(year, month - 1, 1).apply {
                        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    } + Duration(1) - Duration(millis = 0.1)).timeInMillis
            )


            resp.body()?.let {
                it.forEach {
                    it.apply {
                        val lesson = Lesson(moduleName, moduleCode, location, lessonType,
                                startTime.toCalendar(),
                                endTime.toCalendar(),
                                id)
                        schedule.add(lesson)
                        val cachedLesson = lesson.let {
                            CachedLesson(
                                    it.base24ID,
                                    it.moduleCode,
                                    it.name,
                                    it.lessonType,
                                    it.location,
                                    it.startTime.timeInMillis,
                                    it.endTime.timeInMillis
                            )
                        }
                        SPCoreLocalDB.lessonDAO().insertLesson(cachedLesson)
                    }
                }
            }
        } else {
            // NO need to refresh cache, just take from local db

            SPCoreLocalDB.lessonDAO().getCachedLessons().forEach {
                it.apply {
                    schedule.add(
                            Lesson(moduleName, moduleCode, location, lessonType,
                                    startTime.toCalendar(),
                                    endTime.toCalendar(),
                                    base24ID)
                    )
                }
            }
        }

        HardcodedEvents
                .filter { it.startTime.get(Calendar.MONTH) == month - 1 }
                .forEach { schedule.add(it) }

        val now = Calendar.getInstance()
        val qtNow = now.roundUpToNearest(minutes = 10)
        // only add now for the current month
        if (now.get(Calendar.MONTH) == month - 1) {
            schedule.addAll(listOf(
                    Lesson(
                            "SIP",
                            "LC4234",
                            "T1643",
                            "TUT",
                            qtNow - Duration(hours = 1),
                            qtNow + Duration(hours = 1)
                    ),
                    Lesson(
                            "HM2",
                            "SM1337",
                            "T2253",
                            "TUT",
                            qtNow + Duration(hours = 3),
                            qtNow + Duration(hours = 5)
                    )
            ))
        }

        return schedule

        /*
        if(HARDCODE_MODE) {

            val cal = newCalendar(year, month-1, 1)
            for(day in 1..cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                cal.set(Calendar.DAY_OF_MONTH, day)
                if(!cal.isToday() || cal == newCalendar(2018, 1, 15))
                    HardcodedLessons[cal.get(Calendar.DAY_OF_WEEK)]?.forEach {
                        it.apply {
                            schedule.add(
                                    Lesson(moduleName, moduleCode, location, lessonType,
                                            cal.startOfDay() + startTime,
                                            cal.startOfDay() + endTime)
                            )
                        }
                    }
            }

            HardcodedEvents
                    .filter { it.startTime.get(Calendar.MONTH) == month - 1 }
                    .forEach { schedule.add(it) }

            val now = Calendar.getInstance()
            val qtNow = now.roundUpToNearest(minutes = 10)
            // only add now for the current month
            if (now.get(Calendar.MONTH) == month - 1) {
                schedule.addAll(listOf(
                        Lesson(
                            "SIP",
                            "LC4234",
                            "T1643",
                            "TUT",
                            qtNow - Duration(hours = 1),
                            qtNow + Duration(hours = 1)
                        ),
                        Lesson(
                                "HM2",
                                "SM1337",
                                "T2253",
                                "TUT",
                                qtNow + Duration(hours = 3),
                                qtNow + Duration(hours = 5)
                        )
                ))
            }


        }
        */
    }

    fun getEvent(id: Long): Event? {
        if(HARDCODE_MODE) {
            Thread.sleep(200)
            return HardcodedEvents.firstOrNull { it.id == id }
        } else
            TODO("i HaVe CrIpPlInG dEpReSsIoN")
    }

    fun updateEvent(event: Event) {
        if(HARDCODE_MODE) {
            Thread.sleep(200)
            // This works as the Event.equals() and .hashCode() functions are overriden to
            // use Event.id instead
            HardcodedEvents.remove(event)
            HardcodedEvents.add(event)
        } else
            TODO("i HaVe CrIpPlInG dEpReSsIoN")
    }

    fun createEvent(event: Event) {
        if (HARDCODE_MODE) {
            Thread.sleep(200)

            HardcodedEvents.add(event)
        } else
            TODO("i HaVe CrIpPlInG dEpReSsIoN")
    }

    fun deleteEvent(event: Event) {
        if (HARDCODE_MODE) {
            Thread.sleep(200)

            HardcodedEvents.remove(event)
        } else
            TODO("i HaVe CrIpPlInG dEpReSsIoN")
    }

    /**
     * Gets whether the user has been initialized.
     *
     * It will first check using [com.spcore.helpers.Auth.getUserInitializedLocally] to
     * check if the user has been initialized on this device before, if it hasn't, it will
     * check the server.
     */
    fun isUserInitializedOnServer() : Boolean {
        // Just check locally, because this will be automatically updated when performLogin is called
        return Auth.getUserInitializedLocally()
    }

    fun setUserInitializedOnServer(username: String, displayedName: String?) : InitialLoginActivity.SubmitInitStatus {

        val resp = Backend.initializeOrUpdateUserDetails(username, displayedName).execute()

        if (!resp.isSuccessful) {
            resp.errorBody()?.string()?.let {
                val err =
                        backendErrorAdapter.fromJson(it) ?:
                                return InitialLoginActivity.SubmitInitStatus.UNKNOWN_ERROR("Bodyless error")

                return when(err.code) {
                    Backend.DUPLICATE_FOUND -> InitialLoginActivity.SubmitInitStatus.USERNAME_TAKEN
                    else -> InitialLoginActivity.SubmitInitStatus.UNKNOWN_ERROR(err.msg)
                }
            }
        }

        Auth.setUserInitializedLocally(username, displayedName)

        return InitialLoginActivity.SubmitInitStatus.SUCCESS
    }


}