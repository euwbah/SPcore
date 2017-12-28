package com.spcore.models

import android.os.Parcel
import android.os.Parcelable
import com.alamkanak.weekview.WeekViewEvent
import com.spcore.apis.FrontendInterface

class User(val adminNo: String, val username: String, val displayName: String?): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(adminNo)
        parcel.writeString(username)
        parcel.writeString(displayName)
    }

    override fun describeContents() = 0

    fun getSchedule(year: Int, month: Int) : List<WeekViewEvent> {
        return FrontendInterface.getSchedule(adminNo, year, month)
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}