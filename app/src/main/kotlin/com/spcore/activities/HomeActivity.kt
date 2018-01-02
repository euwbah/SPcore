package com.spcore.activities

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.view.*
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.spcore.R
import com.spcore.helpers.*

import com.spcore.models.Event
import com.spcore.models.Lesson

import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.android.synthetic.main.home_nav_header.*
import org.jetbrains.anko.startActivity
import java.util.*

class HomeActivity : AppStateTrackerActivity("HomeActivity") {

    private var isAppBarExpanded = false

    private var toggleListener: ActionBarDrawerToggle? = null

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)

        setScheduleViewDate(Calendar.getInstance())

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        title = "My Timetable"

        toggleListener = ActionBarDrawerToggle(this, home_drawer_layout, toolbar, R.string.nav_tts_open, R.string.nav_tts_close)
        home_drawer_layout.addDrawerListener(toggleListener!!)
        toggleListener!!.syncState()

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


        toolbar_dropdown_calendar.setLocale(TimeZone.getDefault(), Locale.getDefault())
        toolbar_dropdown_calendar.setShouldDrawDaysHeader(true)
        toolbar_dropdown_calendar.setListener(object : CompactCalendarView.CompactCalendarViewListener {
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
                        date_picker_dropdown_button,
                        home_app_bar_layout,
                        isAppBarExpanded,
                        date_picker_arrow,
                        schedule_view)

        // Note: month here is 1-based
        schedule_view.setMonthChangeListener {
            year, month ->
                Auth.user.getSchedule(year, month)
        }

        schedule_view.setScrollListener {
            newFirstVisibleDay, oldFirstVisibleDay ->
                setCalendarDate(newFirstVisibleDay)
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

        schedule_view.hourHeight = Resources.getSystem().displayMetrics.heightPixels / 15

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

    private fun setScheduleViewDate(cal: Calendar) {
        schedule_view.goToDate(cal)
        schedule_view.goToEarliestVisibleEvent(2.0)
        setMYTextView(cal)
    }


    private fun setCalendarDate(cal: Calendar) {
        setMYTextView(cal)
        toolbar_dropdown_calendar.setCurrentDate(cal.toDate())
    }

    private fun setMYTextView(cal: Calendar) {
        month_year_text_view.text = FULL_MONTH_YEAR_DATE_FORMAT.format(cal.toDate())
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
                startActivity(Intent(this, InvitationActivity::class.java))
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
