package com.spcore.activities

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.inputmethod.EditorInfo
import com.spcore.helpers.*
import com.spcore.R
import com.spcore.apis.FrontendInterface
import com.spcore.fragments.DatePickerFragment
import com.spcore.fragments.TimePickerFragment
import com.spcore.models.Event
import com.spcore.models.User
import kotlinx.android.synthetic.main.activity_event_create_update.*
import kotlinx.android.synthetic.main.content_event_create_update.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.alert
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast
import java.util.*
import kotlin.collections.ArrayList

const val RC_EVENT_DELETED = 2
const val RC_EVENT_UPDATE_CANCELLED = 0
const val RC_EVENT_UPDATED = 1

class EventCreateUpdateActivity : AppStateTrackerActivity("EventCreateUpdateActivity"),
                                  DatePickerFragment.DateSetListener,
                                  TimePickerFragment.TimeSetListener{

    // These fragment references are only necessary to prevent memory leaks
    private var datePicker: DatePickerFragment? = null
    private var timePicker: TimePickerFragment? = null


    private var _start: Calendar? = null
    private var start: Calendar
        get() = _start ?: event!!.startTime
        set(value) {
            _start = value
            event_crud_start_time_input.textStr = value.getHumanReadableTime(false)
            event_crud_start_date_input.textStr = value.getHumanReadableDate(true)
        }
    private var _end: Calendar? = null
    private var end: Calendar
        get() = _end ?: event!!.endTime
        set(value) {
            _end = value
            event_crud_end_time_input.textStr = value.getHumanReadableTime(false)
            event_crud_end_date_input.textStr = value.getHumanReadableDate(true)
        }

    private var invitedGuests: MutableList<User>? = null

    /** This property will be null if not in "update" mode */
    private var event: Event? = null

    private val mode: String
        get() = intent.extras.getString("mode")


    // It can be safely assumed that only one of either newStart/End or event will be
    // null at any point in time, hence the following aggregation properties.
    // (newStart/End will be auto-assigned to the current time in "create" mode)

    /*
        Note: Do NOT mutate any of the event's properties until
        the save button is clicked. Room persistent storage is planned
        for the future and the Event models along with the other models will
        cause the local cache to update upon reassignment.
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_create_update)

        setSupportActionBar(event_crud_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        if(mode == "update") {
            event = intent.getParcelableExtra("event")
            event_crud_delete_button.visibility = View.VISIBLE
            initUpdateMode()
        } else if (mode == "create") {
            initCreateMode()
        }

        event_crud_start_date_input.setOnClickListener {
            datePicker = DatePickerFragment.newInstance(start.startOfDay().timeInMillis)
            datePicker?.show(supportFragmentManager, "start")
        }

        event_crud_end_date_input.setOnClickListener {
            datePicker = DatePickerFragment.newInstance(end.startOfDay().timeInMillis)
            datePicker?.show(supportFragmentManager, "end")
        }

        event_crud_start_time_input.setOnClickListener {
            timePicker = TimePickerFragment.newInstance(start.getTimeAsDuration())
            timePicker?.show(supportFragmentManager, "start")
        }

        event_crud_end_time_input.setOnClickListener {
            timePicker = TimePickerFragment.newInstance(end.getTimeAsDuration())
            timePicker?.show(supportFragmentManager, "end")
        }

        event_crud_invite_button.setOnClickListener {
            startActivityForResult<InvitationActivity>(1337,
                    "event" to event,
                    "invite list" to invitedGuests)
        }

        event_crud_delete_button.setOnClickListener {
            alert("Are you sure you want to delete this event?") {
                positiveButton("Confirm"){
                    setResult(RC_EVENT_DELETED)
                    finish()
                }
                negativeButton("Cancel"){}
            }.show()
        }

        event_crud_cancel_button.setOnClickListener {
            cancel()
        }
    }

    private fun initCreateMode() {
        event_crud_toolbar_title.apply {
            textStr = "New Event"
            requestFocus()
        }

        start = Calendar.getInstance().roundUpToNearest(hours = 1)
        end = (Calendar.getInstance() + Duration(hours = 1)).roundUpToNearest(hours = 1)

        event_crud_save_button.setOnClickListener {

            if (!isInputValid()) {
                return@setOnClickListener
            }

            event = Event(
                    event_crud_toolbar_title.textStr.trim(),
                    event_crud_description_input.textStr.trim(),
                    event_crud_location_input.textStr.trim(),
                    start,
                    end,
                    Auth.user,
                    going = arrayListOf(Auth.user),
                    haventReplied =
                        invitedGuests?.let { ArrayList(it) } ?: arrayListOf())

            async(UI) {
                val asyncCreateEvent = bg {
                    FrontendInterface.createEvent(event!!)
                }

                event_crud_save_button.visibility = View.GONE
                event_crud_progress_indicator.visibility = View.VISIBLE

                asyncCreateEvent.await()

                event_crud_save_button.visibility = View.VISIBLE
                event_crud_progress_indicator.visibility = View.GONE

                finish()

                startActivity<EventDetailsActivity>("event" to event)
            }
        }
    }

    private fun initUpdateMode() {
        // Shadow global nullable event with asserted non-null version because in
        // no case should event be null in here
        val event = event!!

        event_crud_toolbar_title.textStr = event.name
        event_crud_location_input.textStr = event.location
        event_crud_description_input.textStr = event.description

        // The following assignments are used to update the UI EditText,
        // so DON'T DELETE THEM even though they look stupid
        start = start
        end = end

        event_crud_save_button.setOnClickListener {

            if (!isInputValid()) {
                return@setOnClickListener
            }

            event.apply {
                name = event_crud_toolbar_title.textStr.trim()
                location = event_crud_location_input.textStr.trim()
                description = event_crud_description_input.textStr.trim()
                startTime = start
                endTime = end

                if (invitedGuests != null) {
                    // An update to the invites list has been done

                    val invitedGuests = invitedGuests!!

                    val ogCombi = listOf(going, notGoing, haventReplied)
                    val og = going + notGoing + haventReplied

                    // Deleted users: Users in the original set that aren't in the invitedGuests list anymore
                    val deleted = og.filter { it !in invitedGuests }

                    // Added users: Invited guests that weren't in the original set of users
                    val added = invitedGuests.filter { it !in og }

                    deleted.forEach { u ->
                        ogCombi.forEach { it.remove(u) }
                    }

                    haventReplied.addAll(added)
                }

                setResult(RC_EVENT_UPDATED,
                        intent.putExtra("event", this)
                )
            }

            event_crud_progress_indicator.visibility = View.VISIBLE
            event_crud_save_button.visibility = View.GONE

            async(UI) {
                val updater = bg { FrontendInterface.updateEvent(event) }

                updater.await()

                event_crud_progress_indicator.visibility = View.GONE
                event_crud_save_button.visibility = View.VISIBLE

                finish()
            }
        }
    }

    private fun isInputValid(): Boolean {
        var ok = true

        event_crud_toolbar_title.apply {
            if (textStr.isBlank()) {
                ok = false
                error = Html.fromHtml("<font color='#eeeeee'>Title is required</font>")
                requestFocus()
            } else
                error = null
        }

        return ok
    }

    override fun onDatePicked(calendar: Calendar, tag: String) {
        when(tag) {
            "start" -> {
                val prevStart = start
                start = calendar + start.getTimeAsDuration()
                if (start >= end) // see explanation in onTimePicked
                    end += start - prevStart
            }
            "end" -> {
                val prevEnd = end
                end = calendar + end.getTimeAsDuration()
                if(end <= start) // see explanation in onTimePicked
                    start += end - prevEnd
            }
        }
    }

    override fun onTimePicked(duration: Duration, tag: String) {
        when(tag) {
            "start" -> {
                val prevStart = start

                start = start.startOfDay() + duration

                if (start >= end)
                    // If `start` happens to be equal/after `end`, postpone `end` such that the duration between
                    // between `end` and `start` was such as before `start` was modified
                    end += start - prevStart
            }
            "end" -> {
                val prevEnd = end

                end = end.startOfDay() + duration

                if(end <= start)
                    // If `end` happens to be equal/before `start`, make `start` earlier such that the duration between
                    // between `start` and `end` was such as before `end` was modified

                    // NOTE: even though += is used, the value of `end - prevEnd` is actually NEGATIVE so
                    // `start` actually gets shifted earlier
                    start += end - prevEnd
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode != RC_INVITE_UPDATE_CANCELLED && data != null) {
            val inviteList = data.getParcelableArrayListExtra<User>("invite list")

            invitedGuests = inviteList
        }
    }

    override fun onBackPressed() {
        cancel()
    }

    fun cancel() {
        when(mode) {
            "update" -> {
                setResult(RC_EVENT_UPDATE_CANCELLED,
                        intent.putExtra("refresh", false))
                finish()
            }
            "create" -> {
                finish()
            }
        }
    }
}
