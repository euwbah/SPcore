package com.spcore.activities

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
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
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.startActivity
import java.util.*

const val TAG_ID_CCV_CURRDATE = 156376132

class HomeActivity : AppStateTrackerActivity("HomeActivity"),
                     AnkoLogger {

    private var isAppBarExpanded = false

    private var toggleListener: ActionBarDrawerToggle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        title = "My Timetable"

        toggleListener = ActionBarDrawerToggle(this, home_drawer_layout, toolbar, R.string.nav_tts_open, R.string.nav_tts_close)
        home_drawer_layout.addDrawerListener(toggleListener!!)
        toggleListener!!.syncState()

        nav_view.setCheckedItem(R.id.nav_day_view)

        nav_view.setNavigationItemSelectedListener navHandler@ {

            val id = it.itemId

            when(id) {
                R.id.nav_day_view -> {
                    setGoToEarliestVisibleEventLoadTrigger()
                    schedule_view.numberOfVisibleDays = 1
                    schedule_view.goToEarliestVisibleEvent(2.0)
                }
                R.id.nav_5_day_view -> {
                    setGoToEarliestVisibleEventLoadTrigger()
                    schedule_view.numberOfVisibleDays = 5
                    schedule_view.goToEarliestVisibleEvent(2.0)

                    // TODO: Automatically shift the 5 weekdays in view if the current selected date on the schedule view
                    // is a weekday. Otherwise, put the current selected date as the 3rd visible date (as the middle)
                }
                R.id.nav_friends -> {
                    startActivity<FriendsActivity>()
                }
                R.id.nav_logout -> {
                    Auth.logout()
                    this@HomeActivity.startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                }
            }

            home_drawer_layout.closeDrawer(nav_view)

            // TODO: Only return true if nav selection is to appear highlighted, if not, return false
            return@navHandler R.id.nav_day_view _or
                                R.id.nav_5_day_view _is id
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
                val cal = dateClicked.toCalendar()
                setScheduleViewDate(cal)
                toolbar_dropdown_calendar.setTag(TAG_ID_CCV_CURRDATE, cal)
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
            // NOTE: WeekView.ScrollListener will activate whenever its displayed date(s) changes,
            // whether the result of a direct or indirect action. This means selecting a date
            // on the toolbar_dropdown_calendar, which in turn calls setScheduleViewDate,
            // will ultimately cause ScrollListener to be evoked. (Which results in a repeat call of
            // setCalendarDate). Hence this check is necessary to avoid utter failure especially
            // when it comes to the synchronicity of cueing and triggering goToEarliestVisibleEvent

            if ((toolbar_dropdown_calendar.getTag(TAG_ID_CCV_CURRDATE) as? Calendar)?.startOfDay() != newFirstVisibleDay.startOfDay())
                setCalendarDate(newFirstVisibleDay)
        }

        schedule_view.setOnEventClickListener { event, eventRect ->
            val intent =
                    when(event) {
                        is Lesson -> Intent(this, LessonDetailsActivity::class.java)
                        is Event -> Intent(this, EventDetailsActivity::class.java)
                        else -> return@setOnEventClickListener
                    }

            intent.putExtra("event", event)

            startActivity(intent)
        }

        schedule_view.hourHeight = Resources.getSystem().displayMetrics.heightPixels / 15

        schedule_view.eventMarginVertical = 1.5.dpToPx().toInt()

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

    /**
     * Method body is self-explanatory
     */
    private fun setGoToEarliestVisibleEventLoadTrigger() {
        debug("event loaded listener cued")
        schedule_view.setEventsLoadedListener {
            // once-off event
            schedule_view.goToEarliestVisibleEvent(2.0)
            removeGoToEarliestVisibleEventLoadTrigger()

            debug("event loaded listener triggered")
        }
    }

    private fun removeGoToEarliestVisibleEventLoadTrigger() {
        schedule_view.eventsLoadedListener = null

        debug("event loaded listener trigger cancelled")
    }

    private fun setScheduleViewDate(cal: Calendar) {
        setGoToEarliestVisibleEventLoadTrigger()
        schedule_view.goToDate(cal)
        setMYTextView(cal)
    }


    private fun setCalendarDate(cal: Calendar) {
        debug("setCalendarDate")
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
