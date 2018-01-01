package com.spcore.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.spcore.helpers.*
import com.spcore.R
import com.spcore.fragments.DatePickerFragment
import com.spcore.fragments.TimePickerFragment
import com.spcore.models.Event
import kotlinx.android.synthetic.main.activity_event_create_update.*
import kotlinx.android.synthetic.main.content_event_create_update.*
import java.util.*

class EventCreateUpdateActivity : AppStateTrackerActivity("EventCreateUpdateActivity"),
                                  DatePickerFragment.DateSetListener,
                                  TimePickerFragment.TimeSetListener {
    // These fragment references are only necessary to prevent memory leaks
    private var datePicker: DatePickerFragment? = null

    private var timePicker: TimePickerFragment? = null
    private var newStart: Calendar? = null

    private var newEnd: Calendar? = null
    private lateinit var event: Event

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

        event = intent.getParcelableExtra("event")

        if(intent.extras.getString("mode") == "update")
            initUpdateMode()

    }

    private fun initUpdateMode() {
        event_crud_toolbar_title.textStr = event.name
        event_crud_location_input.textStr = event.location
        event_crud_description_input.textStr = event.description
        event_crud_start_date_input.textStr = event.startTime.getHumanReadableDate(true)
        event_crud_start_time_input.textStr = event.startTime.getHumanReadableTime(false)
        event_crud_end_date_input.textStr = event.endTime.getHumanReadableDate(true)
        event_crud_end_time_input.textStr = event.endTime.getHumanReadableTime(false)

        event_crud_start_date_input.setOnClickListener {
            datePicker = DatePickerFragment.newInstance(
                    (newStart ?: event.startTime).startOfDay().timeInMillis
            )
            datePicker?.show(supportFragmentManager, "start")
        }

        event_crud_end_date_input.setOnClickListener {
            datePicker = DatePickerFragment.newInstance(
                    (newEnd ?: event.endTime).startOfDay().timeInMillis
            )
            datePicker?.show(supportFragmentManager, "end")
        }

        event_crud_start_time_input.setOnClickListener {
            timePicker = TimePickerFragment.newInstance(
                    (newStart ?: event.startTime).getTimeAsDuration())

            timePicker?.show(supportFragmentManager, "start")
        }

        event_crud_end_time_input.setOnClickListener {
            timePicker = TimePickerFragment.newInstance(
                    (newEnd ?: event.endTime).getTimeAsDuration())

            timePicker?.show(supportFragmentManager, "end")
        }

        event_crud_cancel_button.setOnClickListener {
            cancel()
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

    override fun onDatePicked(calendar: Calendar, tag: String) {
        when(tag) {
            "start" -> {
                newStart = calendar + (newStart ?: event.startTime).getTimeAsDuration()
                event_crud_start_date_input.textStr = newStart?.getHumanReadableDate(true) ?: "NPE"
            }
            "end" -> {
                newEnd = calendar + (newEnd ?: event.endTime).getTimeAsDuration()
                event_crud_end_date_input.textStr = newEnd?.getHumanReadableDate(true) ?: "NPE"
            }
        }
    }

    override fun onTimePicked(duration: Duration, tag: String) {
        when(tag) {
            "start" -> {
                newStart = (newStart ?: event.startTime).startOfDay() + duration
                event_crud_start_time_input.textStr = newStart?.getHumanReadableTime(false) ?: "NPE"
            }
            "end" -> {
                newEnd = (newEnd ?: event.endTime).startOfDay() + duration
                event_crud_end_time_input.textStr = newEnd?.getHumanReadableTime(false) ?: "NPE"
            }
        }
    }

    override fun onBackPressed() {
        cancel()
    }

    fun cancel() {
        setResult(UPDATE_EVENT_DETAILS,
                intent.putExtra("refresh", false))
        finish()
    }
}
