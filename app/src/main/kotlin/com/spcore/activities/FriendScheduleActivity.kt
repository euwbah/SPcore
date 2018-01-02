package com.spcore.activities

import android.content.res.Resources
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.spcore.R
import com.spcore.helpers.*
import com.spcore.models.User

import kotlinx.android.synthetic.main.activity_friend_schedule.*
import kotlinx.android.synthetic.main.content_friend_schedule.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
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
                val cal = dateClicked.toCalendar()
                setScheduleViewDate(cal)
                friend_toolbar_dropdown_calendar.setTag(TAG_ID_CCV_CURRDATE, cal)
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

            // See HomeActivity schedule_view.setScrollListener for explanation
            val calendarViewCurrDate =
                    (friend_toolbar_dropdown_calendar.getTag(TAG_ID_CCV_CURRDATE) as? Calendar)?.startOfDay()
            if (calendarViewCurrDate != newFirstVisibleDay.startOfDay())
                setCalendarDate(newFirstVisibleDay)
        }

        friend_schedule_view.hourHeight = Resources.getSystem().displayMetrics.heightPixels / 15

        friend_schedule_view.eventMarginVertical = 1.5.dpToPx().toInt()

        async(UI) {
            // The delay is necessary as the week view needs some time to load
            // in order for goToEarliestEvent and its related event listeners
            // to function reliably.

            // There's no other way to fix this other than to rewrite the
            // WeekView package from scratch with more robust and canonical synchronicity,
            // obviously I'm not gonna do that so guess we'll have to deal with this stupid
            // hack.
            delay(40)
            setScheduleViewDate(Calendar.getInstance())
        }
    }

    /**
     * Method body is self-explanatory
     */
    private fun setGoToEarliestVisibleEventLoadTrigger() {
        friend_schedule_view.setEventsLoadedListener {
            // once-off event
            friend_schedule_view.goToEarliestVisibleEvent(2.0)
            removeGoToEarliestVisibleEventLoadTrigger()
        }
    }

    private fun removeGoToEarliestVisibleEventLoadTrigger() {
        friend_schedule_view.eventsLoadedListener = null
    }

    private fun setScheduleViewDate(cal: Calendar) {
        setGoToEarliestVisibleEventLoadTrigger()
        friend_schedule_view.goToDate(cal)
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
