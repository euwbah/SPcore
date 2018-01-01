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
import kotlinx.android.synthetic.main.activity_event_details.*
import kotlinx.android.synthetic.main.content_event_details.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
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

        edit_event_fab.setOnClickListener {
            startActivityForResult(
                    Intent(this, EventCreateUpdateActivity::class.java)
                            .putExtra("mode", "update")
                            .putExtra("event", event),

                    UPDATE_EVENT_DETAILS
            )
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
            val adapters = bg {
                listOf(
                        UserProfileListAdapter(this@EventDetailsActivity, event.going),
                        UserProfileListAdapter(this@EventDetailsActivity, event.notGoing),
                        UserProfileListAdapter(this@EventDetailsActivity, event.haventReplied),
                        UserProfileListAdapter(this@EventDetailsActivity, event.deletedInvite)
                )
            }

            val (goingAdapter,
                    notGoingAdapter,
                    haventRepliedAdapter,
                    deletedInviteAdapter) = adapters.await()

            event_details_going_lv.adapter = goingAdapter
            event_details_not_going_lv.adapter = notGoingAdapter
            event_details_havent_replied_lv.adapter = haventRepliedAdapter
            event_details_deleted_invite_lv.adapter = deletedInviteAdapter

            forceListViewsHeightToWrapContent()
        }

        event_details_toolbar_title.text = event.name
        event_details_time_text.text = humanReadableTimeRange(event.startTime, event.endTime)

        event_details_location_text.apply {
            if(event.location != null && event.location.isNotBlank())
                text = event.location
            else
                visibility = View.GONE
        }

        event_details_desc_text.apply {
            if(event.description.isNotBlank())
                text = event.description
            else
                visibility = View.GONE
        }

        if(event.going.size != 0)
            event_details_going_text.text = "Going (${event.going.size})"
        else {
            event_details_going_text.visibility = View.GONE
            event_details_going_lv.visibility = View.GONE
        }

        if(event.notGoing.size != 0)
            event_details_not_going_text.text = "Not Going (${event.notGoing.size})"
        else {
            event_details_not_going_text.visibility = View.GONE
            event_details_not_going_lv.visibility = View.GONE
        }

        if(event.haventReplied.size != 0)
            event_details_havent_replied_text.text = "Not Responded (${event.haventReplied.size})"
        else {
            event_details_havent_replied_text.visibility = View.GONE
            event_details_havent_replied_lv.visibility = View.GONE
        }

        if(event.deletedInvite.size != 0)
            event_details_deleted_invite_text.text = "Declined Invite (${event.deletedInvite.size})"
        else {
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
        if(requestCode == UPDATE_EVENT_DETAILS) {
            event = data.getParcelableExtra("event")

            updateUI()
        }
    }
}
