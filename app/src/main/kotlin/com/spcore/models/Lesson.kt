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

/**
 * Parcelling implementation:
 *
 * 1. WeekViewEvent (Parcelable Object)
 * 2. moduleCode (String)
 */
class Lesson : WeekViewEvent, Parcelable {

    val moduleCode: String
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
                start: Calendar,
                end: Calendar,
                id: Int = Objects.hash(moduleCode, location, start)
    ) : super(id.toLong(), moduleName, location, start, end) {
        this.moduleCode = moduleCode
    }

    constructor(x: Parcel) : super(x) {
        this.moduleCode = x.readString()
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
    fun isNow() : Boolean {
        return Calendar.getInstance() isFrom startTime upTo endTime
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeString(moduleCode)
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