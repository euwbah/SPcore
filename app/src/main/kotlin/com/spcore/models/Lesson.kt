package com.spcore.models

import android.os.Parcel
import android.os.Parcelable
import com.alamkanak.weekview.WeekViewEvent
import com.spcore.R
import com.spcore.helpers.Auth
import com.spcore.helpers.Duration
import com.spcore.helpers.isFrom
import com.spcore.helpers.minus
import java.util.*

/** Shared with [Event] also */
const val         UPCOMING_EVENT_COLOUR                 = 0xff_33_55_ff.toInt()

private const val DEFAULT_LESSON_COLOUR                 = 0xff_44_77_dd.toInt()
private const val ONGOING_LESSON_UNSUBMITTED_ATS_COLOUR = 0xff_dd_55_22.toInt()
private const val ONGOING_LESSON_SUBMITTED_ATS_COLOUR   = 0xff_aa_44_aa.toInt()


/**
 * Parcelling implementation:
 *
 * 1. WeekViewEvent (Parcelable Object)
 * 2. moduleCode (String)
 */
class Lesson : WeekViewEvent, Parcelable, Nowable {

    val moduleCode: String
    val lessonType: String
    val atsKeyed = false


    /**
     * @param moduleName    e.g. MAPP
     * @param moduleCode    e.g. ST1234
     * @param location      e.g. T1234
     * @param start         Lesson start time
     * @param end           Lesson end time
     * @param id            Unique lesson identifier (this value should be returned from server)
     *                      The ID should be unique to module, location, and start time
     */
    constructor(moduleName: String,
                moduleCode: String,
                location: String,
                lessonType: String,
                start: Calendar,
                end: Calendar,
                id: Int = Objects.hash(moduleCode, location, start)
    ) : super(id.toLong(), moduleName, location, start, end) {
        this.moduleCode = moduleCode
        this.lessonType = lessonType
        this.color = DEFAULT_LESSON_COLOUR
    }

    constructor(x: Parcel) : super(x) {
        this.moduleCode = x.readString()
        this.lessonType = x.readString()
        this.color = DEFAULT_LESSON_COLOUR
    }

    /**
     * Determines whether ATS can be keyed in now
     *
     * IMPORTANT: Note that lessons which are one after another will have overlapping
     *            ATS-keyable timeframes. In such scenarioes, only regard the one that comes after.
     */
    fun isATSKeyableNow() : Boolean {
        return Calendar.getInstance() isFrom startTime - Duration(minutes = 15) upTo endTime
    }

    /**
     * Determines whether the lesson is currently on-going
     */
    override fun isNow() : Boolean {
        return Calendar.getInstance() isFrom startTime upTo endTime
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeString(moduleCode)
        dest.writeString(lessonType)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Lesson> {
        override fun createFromParcel(parcel: Parcel): Lesson {
            return Lesson(parcel)
        }

        override fun newArray(size: Int): Array<Lesson?> {
            return arrayOfNulls(size)
        }
    }

}

fun List<WeekViewEvent>.getCurrentATSKeyableLessons() : List<Lesson>? {

    val ATSKeyableLessons = this
            .filter {
                it is Lesson &&
                        it.isATSKeyableNow()
            }
            .map {
                it as Lesson
            }

    return if (ATSKeyableLessons.isEmpty())
        null
    else
        ATSKeyableLessons
}