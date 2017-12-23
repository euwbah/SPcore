package com.spcore.activities

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.spcore.R
import com.spcore.helpers.FULL_MONTH_YEAR_DATE_FORMAT
import com.spcore.helpers.toCalendar
import com.spcore.services.FrontendInterface

import com.spcore.listeners.AppBarStateListener
import com.spcore.models.Lesson

import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*
import java.util.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        title = "My Timetable"

        val toggleListener = ActionBarDrawerToggle(this, home_drawer_layout, toolbar, R.string.nav_tts_open, R.string.nav_tts_close)
        home_drawer_layout.addDrawerListener(toggleListener)

        toggleListener.syncState()

        nav_view.setNavigationItemSelectedListener navHandler@ {

            val id = it.itemId

            when(id) {
                R.id.nav_day_view ->
                        schedule_view.numberOfVisibleDays = 1
                R.id.nav_week_view ->
                        schedule_view.numberOfVisibleDays = 7
//                R.id.nav_logout ->
//                        initJWTTokenSP().edit().remove("token").apply()
//                        this@HomeActivity.startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
            }

            // TODO: Only return true if nav selection is to appear highlighted, if not, return false
            return@navHandler true
        }

        toolbar_dropdown_calendar.setLocale(TimeZone.getDefault(), Locale.ENGLISH)
        toolbar_dropdown_calendar.setShouldDrawDaysHeader(true)
        toolbar_dropdown_calendar.setListener(object : CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                setScheduleViewDate(dateClicked)
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                setMYTextView(firstDayOfNewMonth)
            }
        })

        // Set to today
        setCalendarDate(Date())

        run show_hide_calendar_AppBar@ {
            var isExpanded = false
            var appBarState: AppBarStateListener.State = AppBarStateListener.State.COLLAPSED

            date_picker_dropdown_button.setOnClickListener {
                isExpanded = !isExpanded
                home_app_bar_layout.setExpanded(isExpanded, true)
            }

            home_app_bar_layout.addOnOffsetChangedListener(
                AppBarStateListener {
                    state, prev ->
                        appBarState = state
//                        Log.d("STATE", state.toString())
                        when(state) {
                            is AppBarStateListener.State.COLLAPSED -> {
                                date_picker_arrow.rotation = -180f
                                isExpanded = false

                                schedule_view.invalidate()
                            }
                            is AppBarStateListener.State.EXPANDED -> {
                                date_picker_arrow.rotation = 0f
                                isExpanded = true

                                schedule_view.invalidate()
                            }
                            is AppBarStateListener.State.QUANTUM_FLUX_SUPERPOSITION -> {
                                date_picker_arrow.rotation = (state.expandedness - 1).toFloat() * 180
                            }
                            is AppBarStateListener.State.STUCK_IN_FUTURE_TIMELINE_SUPERPOSITION -> {

                                // It is logical to assusme that if the user wishes to interact
                                // with the AppBarLayout, the user's ultimate intention would be to
                                // toggle the calendar view. As such, the threshold of which a
                                // semi-collapsed/expanded toolbar should constitute as
                                // "to be expanded" or "to be collapsed" should differ
                                // according to the previous stable state of the AppBarLayout

                                val expandingThreshold =
                                        if (prev is AppBarStateListener.State.COLLAPSED)
                                            0.2
                                        else
                                            0.8

                                val toExpand = state.originalExpandedness >= expandingThreshold
                                isExpanded = toExpand
                                home_app_bar_layout.setExpanded(toExpand, true)
                            }
                        }
                }
            )

            val handleAppBarScrollFinished = foo@ {
                view: View, motionEvent: MotionEvent ->
                    Log.d("MotionEvent", motionEvent.toString())
                    if (motionEvent.actionMasked == MotionEvent.ACTION_CANCEL) {
                        // When the user lets go of the dropdown calendar...

                        if (appBarState is AppBarStateListener.State.QUANTUM_FLUX_SUPERPOSITION) {
                            try {
                                val superposition =
                                        appBarState as AppBarStateListener.State.QUANTUM_FLUX_SUPERPOSITION

                                val toExpand = superposition.expandedness >= 0.5

                                isExpanded = toExpand
                                home_app_bar_layout.setExpanded(toExpand, true)

                                date_picker_dropdown_button.invalidate()
                            } catch(e: TypeCastException) {
                                // Ignore the TypeCastException
                            }
                        }

                        return@foo true
                    }

                    return@foo false
            }

            // It is also possible to drag from the dropdown button to collapse/expand
            // the action bar
//            date_picker_dropdown_button.setOnTouchListener listener@ {
//                view, motionEvent ->
//                    return@listener handleAppBarScrollFinished(view, motionEvent)
//            }

//            toolbar_dropdown_calendar.setOnTouchListener listener@ {
//                view, motionEvent ->
//                    return@listener handleAppBarScrollFinished(view, motionEvent)
//            }
        }

        // Note: month here is 1-based
        schedule_view.setMonthChangeListener {
            year, month ->
                FrontendInterface.getSchedule(year, month)
        }

        schedule_view.setScrollListener {
            newFirstVisibleDay, oldFirstVisibleDay ->
                setCalendarDate(newFirstVisibleDay.time)
        }

        schedule_view.setOnEventClickListener { event, eventRect ->
            startActivity(
                    if (event is Lesson) {
                        val intent = Intent(this, LessonDetailsActivity::class.java)
                        intent.putExtra("event", event)
                        intent
                    } else {
                        TODO("Unsupported event type")
                    }
            )
        }

        schedule_view.hourHeight = Resources.getSystem().getDisplayMetrics().heightPixels / 15

        Handler().postDelayed({
            // Give some time to load the hard-coded data before snapping the earliest visible event
            // into view
            setScheduleViewDate(Date())
        }, 50)

    }

    private fun setScheduleViewDate(date: Date) {
        val cal = date.toCalendar()
        schedule_view.goToDate(cal)
        schedule_view.goToEarliestVisibleEvent()
        setMYTextView(date)
    }


    private fun setCalendarDate(date: Date) {
        setMYTextView(date)
        toolbar_dropdown_calendar.setCurrentDate(date)
    }

    private fun setMYTextView(date: Date) {
        month_year_text_view.text = FULL_MONTH_YEAR_DATE_FORMAT.format(date)
    }

    override fun setTitle(newTitle: CharSequence) {
        title_text_view.text = newTitle
    }

    override fun onBackPressed() {
        // Check if the drawer, on which ever way is left (Start) relative to the
        // current screen orientation, is left.

        if(home_drawer_layout.isDrawerOpen(GravityCompat.START))
            home_drawer_layout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }
}
