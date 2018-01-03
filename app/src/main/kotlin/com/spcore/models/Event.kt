package com.spcore.models

import android.os.Parcel
import android.os.Parcelable
import com.alamkanak.weekview.WeekViewEvent
import com.spcore.helpers.isFrom
import java.util.*
import kotlin.collections.ArrayList


private const val UNRESPONDED_EVENT_COLOUR = 0xff_aa_88_33.toInt()
private const val GOING_EVENT_COLOUR       = 0xff_33_aa_55.toInt()
private const val NOT_GOING_EVENT_COLOUR   = 0xff_22_22_22.toInt()

// Note that there isn't any "Deleted invitiation" colour, because the event would simply not exist
// on the schedule

class Event : WeekViewEvent, Parcelable, Nowable {

    var description: String
    val creator: User
    val going: ArrayList<User>
    val notGoing: ArrayList<User>
    val haventReplied: ArrayList<User>
    val deletedInvite: ArrayList<User>

    constructor(parcel: Parcel) : super(parcel) {
        this.description = parcel.readString()
        creator = parcel.readParcelable(User::class.java.classLoader)
        going = parcel.createTypedArrayList(User.CREATOR)
        notGoing = parcel.createTypedArrayList(User.CREATOR)
        haventReplied = parcel.createTypedArrayList(User.CREATOR)
        deletedInvite = parcel.createTypedArrayList(User.CREATOR)
    }

    /**
     * @param eventName     e.g. Jazz Band Horn Sectionals
     * @param eventDesc     e.g. Going through Duration's Notice and Tiny Steps by Jean Coal-Train
     * @param location      e.g. Some shady studio in the middle of nowhere
     * @param start         Event start time
     * @param end           Event end time
     * @param creator  The [User] who created this event
     * @param going         List of [User]s who said they are going
     * @param notGoing      List of [User]s who said they aren't going
     * @param haventReplied List of [User]s who haven't replied
     * @param deletedInvite List of [User]s who have completely removed the event from their schedule view
     * @param id            Unique event identifier
     */
    constructor(eventName: String,
                eventDesc: String,
                location: String,
                start: Calendar,
                end: Calendar,
                creator: User,
                going: ArrayList<User> = arrayListOf(),
                notGoing: ArrayList<User> = arrayListOf(),
                haventReplied: ArrayList<User> = arrayListOf(),
                deletedInvite: ArrayList<User> = arrayListOf(),
                id: Int = Objects.hash(eventName, location, start)
    ) : super(id.toLong(), eventName, location, start, end) {
        this.description = eventDesc
        this.creator = creator
        this.going = going
        this.notGoing = notGoing
        this.haventReplied = haventReplied
        this.deletedInvite = deletedInvite
    }

    fun remove(user: User) {
        listOf(going, notGoing, haventReplied).forEach { it.remove(user) }
    }

    override fun hashCode(): Int {
        return this.id.toInt()
    }

    override fun equals(other: Any?): Boolean {
        return other is Event && other.id == this.id
    }

    /**
     * Simple helper infix function that looks good
     */
    infix fun isCreatedBy(user: User): Boolean {
        return user == creator
    }

    /**
     * Determines whether the event is currently on-going
     */
    override fun isNow() : Boolean {
        return Calendar.getInstance() isFrom startTime upTo endTime
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeString(description)
        parcel.writeParcelable(creator, flags)
        parcel.writeTypedList(going)
        parcel.writeTypedList(notGoing)
        parcel.writeTypedList(haventReplied)
        parcel.writeTypedList(deletedInvite)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Event> {
        override fun createFromParcel(parcel: Parcel): Event {
            return Event(parcel)
        }

        override fun newArray(size: Int): Array<Event?> {
            return arrayOfNulls(size)
        }
    }
}