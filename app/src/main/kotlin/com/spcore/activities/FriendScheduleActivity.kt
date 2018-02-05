package com.spcore.activities

import android.content.res.Resources
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.alamkanak.weekview.WeekViewEvent
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.spcore.R
import com.spcore.apis.FrontendInterface
import com.spcore.helpers.*
import com.spcore.models.User
import kotlinx.android.synthetic.main.activity_friend_schedule.*
import kotlinx.android.synthetic.main.content_friend_schedule.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.info
import java.util.*

class FriendScheduleActivity : AppCompatActivity(),
                               AnkoLogger {

    private lateinit var user: User

    private var isAppBarExpanded = false


    // See HomeActivity.kt for explanation on the following two flags
    private var initialLoadCurrentDayFlag = true
    private var cueJustAddedCurrentDay = false

    private var monthsLoadingOrLoaded: MutableSet<Pair<Int, Int>> = mutableSetOf()

    private val schedule: MutableList<WeekViewEvent> = mutableListOf()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_schedule)
        setSupportActionBar(friend_schedule_toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        friend_schedule_toolbar.setNavigationOnClickListener {
            finish()
        }

        user = intent.getParcelableExtra("user")

        title = "@${user.username}"

        friend_schedule_toolbar_dropdown_calendar.setLocale(TimeZone.getDefault(), Locale.getDefault())
        friend_schedule_toolbar_dropdown_calendar.setShouldDrawDaysHeader(true)
        friend_schedule_toolbar_dropdown_calendar.setListener(object : CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                val cal = dateClicked.toCalendar()
                setScheduleViewDate(cal)
                friend_schedule_toolbar_dropdown_calendar.setTag(TAG_ID_CCV_CURRDATE, cal)
                friend_schedule_app_bar_layout.setExpanded(false, true)
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                setMYTextView(firstDayOfNewMonth.toCalendar())
            }
        })

        // Set to today
        setCalendarDate(Calendar.getInstance())

        isAppBarExpanded =
                initCoolCalendarDropDown(
                        friend_schedule_date_picker_dropdown_button,
                        friend_schedule_app_bar_layout,
                        isAppBarExpanded,
                        friend_schedule_date_picker_arrow,
                        friend_schedule_view)

        friend_schedule_view.setMonthChangeListener {
            year, month ->

            if (initialLoadCurrentDayFlag && cueJustAddedCurrentDay) {
                initialLoadCurrentDayFlag = false

                setGoToEarliestVisibleEventLoadTrigger()
            }

            if (Pair(year, month) !in monthsLoadingOrLoaded) {
                monthsLoadingOrLoaded.add(Pair(year, month))
                cueLoadSchedule(year, month)
                info("NIBBA Cue load $month")
                return@setMonthChangeListener listOf()
            }

            val ret = mutableListOf<WeekViewEvent>()


            schedule.forEach {
                if (it.startTime isFrom newCalendar(year, month - 1, 1) to
                        newCalendar(year, month - 1, 1).apply {
                            set(Calendar.DAY_OF_MONTH, this.getActualMaximum(Calendar.DAY_OF_MONTH))
                        } + Duration(days = 1) - Duration(millis = 0.1))
                    ret.add(it)
            }


            friend_schedule_view.invalidate()
            info("NIBBA invalidated schedule view month: $month")

            //ret.forEach {info("${it.name} : ${it.startTime.getHumanReadableDate(false)}")}
            info("NIBBA Month Change Callback returned (month: $month, size: ${ret.count()})")
            ret
        }

        friend_schedule_view.setScrollListener {
            newFirstVisibleDay, oldFirstVisibleDay ->

            // See HomeActivity schedule_view.setScrollListener for explanation
            val calendarViewCurrDate =
                    (friend_schedule_toolbar_dropdown_calendar.getTag(TAG_ID_CCV_CURRDATE) as? Calendar)?.startOfDay()
            if (calendarViewCurrDate != newFirstVisibleDay.startOfDay())
                setCalendarDate(newFirstVisibleDay)
        }

        friend_schedule_view.hourHeight = Resources.getSystem().displayMetrics.heightPixels / 10

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

    private fun cueLoadSchedule(year: Int, month: Int) {
        async(UI) {
            val asyncSchedule = bg {
                val x = FrontendInterface.getSchedule(user.adminNo, year, month)
                info("NIBBA bg asyncSchedule loaded month: $month")
                x
            }

            schedule.removeAll {
                it.startTime isFrom newCalendar(year, month - 1, 1) to
                        newCalendar(year, month - 1, 1).apply {
                            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                        } + (Duration(days = 1) - Duration(millis = 0.1))
            }

            if (month == Calendar.getInstance().get(Calendar.MONTH) + 1)
                cueJustAddedCurrentDay = true

            val scheduleToAdd = asyncSchedule.await()
            schedule.addAll(scheduleToAdd)

            info("NIBBA asyncSchedule added month: $month")

            Thread.sleep(42)

            friend_schedule_view.notifyDatasetChanged()
            info("NIBBA notifyDatasetChanged month: $month")
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
        friend_schedule_toolbar_dropdown_calendar.setCurrentDate(cal.toDate())
    }

    private fun setMYTextView(cal: Calendar) {
        friend_schedule_month_year_text_view.text = FULL_MONTH_YEAR_DATE_FORMAT.format(cal.toDate())
    }

    override fun setTitle(newTitle: CharSequence) {
        friend_schedule_title_text_view.text = newTitle
    }

    override fun onNavigateUp(): Boolean {
        return super.onNavigateUp()
    }
}
