package com.spcore.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.spcore.R
import com.spcore.helpers.Auth
import com.spcore.helpers.humanReadableTimeRange
import com.spcore.models.Event
import kotlinx.android.synthetic.main.activity_event_details.*
import kotlinx.android.synthetic.main.content_event_details.*

class EventDetailsActivity : AppCompatActivity() {

    private lateinit var event: Event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)
        setSupportActionBar(event_details_toolbar)

        event = intent.extras.getParcelable("event")

        event_details_time_text.text = humanReadableTimeRange(event.startTime, event.endTime)
        event_details_location_text.text = event.location
        event_details_desc_text.text = event.eventDetails


        edit_event_fab.visibility =
                if(event isCreatedBy Auth.user)
                    View.VISIBLE
                else
                    View.GONE

        

    }
}
