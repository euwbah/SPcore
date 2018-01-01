package com.spcore.activities

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.view.*
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.spcore.R
import com.spcore.helpers.Auth
import com.spcore.helpers.FULL_MONTH_YEAR_DATE_FORMAT
import com.spcore.helpers._or
import com.spcore.helpers.toCalendar

import com.spcore.listeners.AppBarStateListener
import com.spcore.models.Event
import com.spcore.models.Lesson

import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.android.synthetic.main.home_nav_header.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity
import java.util.*

class HomeActivity : AppStateTrackerActivity("HomeActivity") {

    private var isAppBarExpanded = false


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
                R.id.nav_5_day_view -> {
                    schedule_view.numberOfVisibleDays = 5
                    // TODO: Automatically shift the 5 weekdays in view if the current selected date on the schedule view
                    // is a weekday. Otherwise, put the current selected date as the 3rd visible date (as the middle)
                }
                R.id.nav_logout -> {
                    Auth.logout()
                    this@HomeActivity.startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                }
            }

            // TODO: Only return true if nav selection is to appear highlighted, if not, return false
            return@navHandler R.id.nav_day_view _or R.id.nav_day_view _is id
        }

        home_drawer_layout.addDrawerListener(object : DrawerLayout.DrawerListener {
            var initHeader = false
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerOpened(drawerView: View) {
                if(!initHeader) {
                    nav_header_image_view.setImageDrawable(Auth.user.getProfilePic(this@HomeActivity))
                    nav_header_username_text.text = "@${Auth.user.username}"
                    nav_header_name_text.text = Auth.user.displayName
                    initHeader = true
                }
            }
        })


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
            var appBarState: AppBarStateListener.State = AppBarStateListener.State.COLLAPSED

            date_picker_dropdown_button.setOnClickListener {
                isAppBarExpanded = !isAppBarExpanded
                home_app_bar_layout.setExpanded(isAppBarExpanded, true)
            }

            home_app_bar_layout.addOnOffsetChangedListener(
                AppBarStateListener {
                    state, prev ->
                        appBarState = state
                        when(state) {
                            is AppBarStateListener.State.COLLAPSED -> {
                                date_picker_arrow.rotation = -180f
                                isAppBarExpanded = false

                                schedule_view.invalidate()
                            }
                            is AppBarStateListener.State.EXPANDED -> {
                                date_picker_arrow.rotation = 0f
                                isAppBarExpanded = true

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
                                isAppBarExpanded = toExpand
                                home_app_bar_layout.setExpanded(toExpand, true)
                            }
                        }
                }
            )
        }

        // Note: month here is 1-based
        schedule_view.setMonthChangeListener {
            year, month ->
                Auth.user.getSchedule(year, month)
        }

        schedule_view.setScrollListener {
            newFirstVisibleDay, oldFirstVisibleDay ->
                setCalendarDate(newFirstVisibleDay.time)
        }

        schedule_view.setOnEventClickListener { event, eventRect ->
            val intent =
                    when(event) {
                        is Lesson -> Intent(this, LessonDetailsActivity::class.java)
                        is Event -> Intent(this, EventDetailsActivity::class.java)
                        else -> TODO("Unsupported event type")
                    }

            intent.putExtra("event", event)

            startActivity(intent)
        }

        schedule_view.hourHeight = Resources.getSystem().getDisplayMetrics().heightPixels / 15

        Handler().postDelayed({
            // Give some time to load the hard-coded data before snapping the earliest visible event
            // into view
            setScheduleViewDate(Date())
        }, 50)

        create_event_fab.setOnClickListener {
            // TODO: Change this to startActivityForResult
            startActivity<EventCreateUpdateActivity>("mode" to "create")
        }
    }

    override fun onResume() {
        super.onResume()
        // TODO: Only notifyDatasetChanged if onActivityResult yields information that a new Event was created
        // this is really really inefficient, but it works for now.
        schedule_view.notifyDatasetChanged()
    }

    private fun setScheduleViewDate(date: Date) {
        val cal = date.toCalendar()
        schedule_view.goToDate(cal)
        schedule_view.goToEarliestVisibleEvent(2.0)
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
        else if(isAppBarExpanded)
            home_app_bar_layout.setExpanded(false, true)
        else
            super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    /**
     * Event handler when action bar items are clicked
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_home_ats -> {
                startActivity(Intent(this,InvitationActivity::class.java))
                true
            }
            R.id.action_home_refresh -> {
                schedule_view.notifyDatasetChanged()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }

    }
}
