package com.spcore.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.spcore.R
import com.spcore.adapters.UserProfileListAdapter
import com.spcore.apis.FrontendInterface
import com.spcore.helpers.Auth
import com.spcore.helpers.humanReadableTimeRange
import com.spcore.helpers.setHeightToWrapContent
import com.spcore.models.Event
import com.spcore.models.User
import kotlinx.android.synthetic.main.activity_event_details.*
import kotlinx.android.synthetic.main.content_event_details.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

const val UPDATE_EVENT_DETAILS = 1

class EventDetailsActivity : AppCompatActivity() {

    private lateinit var event: Event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)

        setSupportActionBar(event_details_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        event = intent.extras.getParcelable("event")

        updateUI()

        event_details_scroll_view.scrollY = 0

        edit_event_fab.setOnClickListener {
            startActivityForResult<EventCreateUpdateActivity>(
                    UPDATE_EVENT_DETAILS,
                    "mode" to "updat",
                    "event" to event)
        }

        listOf(event_details_going_lv, event_details_not_going_lv, event_details_havent_replied_lv,
                event_details_deleted_invite_lv).forEach {
            it.setOnItemClickListener listener@ { adapterView, view, i, l ->
                val user = view.tag as? User ?: return@listener

                // Don't let the user click oneself's profile
                if (user != Auth.user)
                    startActivity<FriendScheduleActivity>("user" to user)
            }
        }

        // Ensure that displayed data is up-to-date

        async(UI) {
            val asyncUpdatedEvent = bg { FrontendInterface.getEvent(event.id) }

            val updatedEvent = asyncUpdatedEvent.await()

            if (updatedEvent == null) {
                toast("Event no longer exists")
                finish()
            } else {
                event = updatedEvent
                updateUI()
            }
        }
    }

    private fun updateUI() {
        async(UI) {
            // TODO: To prevent this absolute horror, make a more generalized ListAdapter which supports
            // multiple data types to allow for automated generation of list headers.
            val adapters = bg {
                val userRoleMapping = mapOf(event.creator to "Organizer")

                listOf(event.going, event.notGoing, event.haventReplied, event.deletedInvite)
                        .map {
                            UserProfileListAdapter(
                                    this@EventDetailsActivity,
                                    it.sortedBy {
                                        // make sure the organizer appears first
                                        if (it == event.creator)
                                            "\u0001"
                                        else
                                            it.username
                                    },
                                    userRoleMapping)
                        }
            }

            val (goingAdapter,
                    notGoingAdapter,
                    haventRepliedAdapter,
                    deletedInviteAdapter) = adapters.await()

            event_details_going_lv.adapter = goingAdapter
            event_details_not_going_lv.adapter = notGoingAdapter
            event_details_havent_replied_lv.adapter = haventRepliedAdapter
            event_details_deleted_invite_lv.adapter = deletedInviteAdapter



            delay(50)
            forceListViewsHeightToWrapContent()
        }

        event_details_toolbar_title.text = event.name
        event_details_time_text.text = humanReadableTimeRange(event.startTime, event.endTime)

        event_details_location_text.apply {
            if(event.location != null && event.location.isNotBlank()) {
                text = event.location
                visibility = View.VISIBLE
            } else
                visibility = View.GONE
        }

        event_details_desc_text.apply {
            if(event.description.isNotBlank()) {
                text = event.description
                visibility = View.VISIBLE
            } else
                visibility = View.GONE
        }

        if(event.going.size != 0) {
            event_details_going_text.text = "Going (${event.going.size})"
            event_details_going_text.visibility = View.VISIBLE
            event_details_going_lv.visibility = View.VISIBLE
        } else {
            event_details_going_text.visibility = View.GONE
            event_details_going_lv.visibility = View.GONE
        }

        if(event.notGoing.size != 0) {
            event_details_not_going_text.text = "Not Going (${event.notGoing.size})"
            event_details_not_going_text.visibility = View.VISIBLE
            event_details_not_going_lv.visibility = View.VISIBLE
        } else {
            event_details_not_going_text.visibility = View.GONE
            event_details_not_going_lv.visibility = View.GONE
        }

        if(event.haventReplied.size != 0) {
            event_details_havent_replied_text.text = "Not Responded (${event.haventReplied.size})"
            event_details_havent_replied_text.visibility = View.VISIBLE
            event_details_havent_replied_lv.visibility = View.VISIBLE
        } else {
            event_details_havent_replied_text.visibility = View.GONE
            event_details_havent_replied_lv.visibility = View.GONE
        }

        if(event.deletedInvite.size != 0) {
            event_details_deleted_invite_text.text = "Declined Invite (${event.deletedInvite.size})"
            event_details_deleted_invite_text.visibility = View.VISIBLE
            event_details_deleted_invite_lv.visibility = View.VISIBLE
        } else {
            event_details_deleted_invite_text.visibility = View.GONE
            event_details_deleted_invite_lv.visibility = View.GONE
        }

        edit_event_fab.visibility =
                if(event isCreatedBy Auth.user)
                    View.VISIBLE
                else
                    View.GONE
    }

    private fun forceListViewsHeightToWrapContent() {
        arrayOf(event_details_going_lv, event_details_deleted_invite_lv, event_details_havent_replied_lv, event_details_not_going_lv)
                .forEach { it.setHeightToWrapContent() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if(resultCode == RC_EVENT_UPDATED) {
            event = data.getParcelableExtra("event")

            updateUI()
        }
    }
}
