package com.spcore.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.spcore.helpers.*
import com.spcore.R
import com.spcore.models.Event
import kotlinx.android.synthetic.main.activity_event_create_update.*
import kotlinx.android.synthetic.main.content_event_create_update.*

class EventCreateUpdateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_create_update)

        setSupportActionBar(event_crud_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        if(intent.extras.getString("mode") == "update")
            initUpdateMode(intent.extras.getParcelable("event"))

        event_crud_start_date_input.setOnClickListener {

        }
    }

    private fun initUpdateMode(event: Event) {
        event_crud_toolbar_title.textStr = event.name
        event_crud_location_input.textStr = event.location
        event_crud_description_input.textStr = event.description
        event_crud_start_date_input.textStr = event.startTime.getHumanReadableDate(true)
        event_crud_start_time_input.textStr = event.startTime.getHumanReadableTime(false)
        event_crud_end_date_input.textStr = event.endTime.getHumanReadableDate(true)
        event_crud_end_time_input.textStr = event.endTime.getHumanReadableTime(false)

        event_crud_cancel_button.setOnClickListener {
            setResult(UPDATE_EVENT_DETAILS,
                    intent.putExtra("refresh", false))
            finish()
        }

        event_crud_save_button.setOnClickListener {

            event.name = event_crud_toolbar_title.textStr.trim()
            event.location = event_crud_location_input.textStr.trim()
            event.description = event_crud_description_input.textStr.trim()


            setResult(UPDATE_EVENT_DETAILS,
                    intent
                            .putExtra("refresh", true)
                            .putExtra("event", event)
            )
        }
    }
}
