package com.spcore.models

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import com.alamkanak.weekview.WeekViewEvent
import com.spcore.R
import com.spcore.apis.FrontendInterface
import com.spcore.helpers.HARDCODE_MODE

open class User(
        val adminNo: String,
        val username: String,
        val displayName: String?,
        val HARDCODE_MODE_friends: ArrayList<User> = arrayListOf()): Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.createTypedArrayList(User.CREATOR))

    fun getSchedule(year: Int, month: Int) : List<WeekViewEvent> {
        return FrontendInterface.getSchedule(adminNo, year, month)
    }

    fun getProfilePic(context: Context): Drawable {
        // TODO: this is HARDCODE_MODE
        return context.getDrawable(R.drawable.ic_profile_pic)
    }

    fun getFriends(): List<User> {
        if(HARDCODE_MODE)
            return HARDCODE_MODE_friends
        else
            TODO("I hAVE CRIPPLing DEpreSSion")
    }

    override fun equals(other: Any?): Boolean {
        return if(other is User)
                    this.adminNo == other.adminNo
                else
                    false
    }

    override fun hashCode(): Int {
        return this.adminNo.toInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(adminNo)
        parcel.writeString(username)
        parcel.writeString(displayName)
        parcel.writeTypedList(HARDCODE_MODE_friends)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}