package com.spcore.activities

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.spcore.R
import com.spcore.helpers.*
import com.spcore.models.Event
import com.spcore.models.Lesson
import com.spcore.models.User

import kotlinx.android.synthetic.main.activity_friend_schedule.*
import kotlinx.android.synthetic.main.content_friend_schedule.*
import java.util.*

class FriendScheduleActivity : AppCompatActivity() {

    private lateinit var user: User

    private var isAppBarExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_schedule)
        setSupportActionBar(friend_toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        user = intent.getParcelableExtra("user")

        title = "@${user.username}"

        friend_toolbar_dropdown_calendar.setLocale(TimeZone.getDefault(), Locale.getDefault())
        friend_toolbar_dropdown_calendar.setShouldDrawDaysHeader(true)
        friend_toolbar_dropdown_calendar.setListener(object : CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                setScheduleViewDate(dateClicked.toCalendar())
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                setMYTextView(firstDayOfNewMonth.toCalendar())
            }
        })

        // Set to today
        setCalendarDate(Calendar.getInstance())

        isAppBarExpanded =
                initCoolCalendarDropDown(
                        friend_date_picker_dropdown_button,
                        friend_app_bar_layout,
                        isAppBarExpanded,
                        friend_date_picker_arrow,
                        friend_schedule_view)

        friend_schedule_view.setMonthChangeListener {
            year, month ->
            Auth.user.getSchedule(year, month)
        }

        friend_schedule_view.setScrollListener {
            newFirstVisibleDay, oldFirstVisibleDay ->
            setCalendarDate(newFirstVisibleDay)
        }
//
//        friend_schedule_view.setOnEventClickListener { event, eventRect ->
//            val intent =
//                    when(event) {
//                        is Lesson -> Intent(this, LessonDetailsActivity::class.java)
//                        is Event -> Intent(this, EventDetailsActivity::class.java)
//                        else -> TODO("Unsupported event type")
//                    }
//
//            intent.putExtra("event", event)
//
//            startActivity(intent)
//        }

        friend_schedule_view.hourHeight = Resources.getSystem().displayMetrics.heightPixels / 15

    }

    private fun setScheduleViewDate(cal: Calendar) {
        friend_schedule_view.goToDate(cal)
        friend_schedule_view.goToEarliestVisibleEvent(2.0)
        setMYTextView(cal)
    }


    private fun setCalendarDate(cal: Calendar) {
        setMYTextView(cal)
        friend_toolbar_dropdown_calendar.setCurrentDate(cal.toDate())
    }

    private fun setMYTextView(cal: Calendar) {
        friend_month_year_text_view.text = FULL_MONTH_YEAR_DATE_FORMAT.format(cal.toDate())
    }

    override fun setTitle(newTitle: CharSequence) {
        friend_title_text_view.text = newTitle
    }

}
