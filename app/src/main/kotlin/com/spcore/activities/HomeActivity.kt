package com.spcore.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.alamkanak.weekview.WeekViewEvent
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.spcore.R
import com.spcore.apis.FrontendInterface
import com.spcore.helpers.*
import com.spcore.models.Event
import com.spcore.models.Lesson
import com.spcore.models.getCurrentATSKeyableLessons
import com.spcore.persistence.SPCoreLocalDB
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.android.synthetic.main.home_nav_header.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.*
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.design.longSnackbar
import java.util.*

const val TAG_ID_CCV_CURRDATE = 156376132
const val BROADCAST_DELETED_EVENT = "com.spcore.broadcasts.refreshscheduleview"

class HomeActivity : AppStateTrackerActivity("HomeActivity"),
                     AnkoLogger {

    private var isAppBarExpanded = false

    private var toggleListener: ActionBarDrawerToggle? = null

    private var monthsLoadingOrLoaded: MutableSet<Pair<Int, Int>> = mutableSetOf()

    private val schedule: MutableList<WeekViewEvent> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        title = "My Timetable"

        toggleListener = ActionBarDrawerToggle(this, home_drawer_layout, toolbar, R.string.nav_tts_open, R.string.nav_tts_close)
        home_drawer_layout.addDrawerListener(toggleListener!!)
        toggleListener!!.syncState()


        ScheduleViewState.getNumberOfVisibleDays().let {
            if (it == 1) {
                nav_view.setCheckedItem(R.id.nav_day_view)
                setGoToEarliestVisibleEventLoadTrigger()
                schedule_view.numberOfVisibleDays = 1
                schedule_view.goToEarliestVisibleEvent(2.0)
                ScheduleViewState.setNumberOfVisibleDays(1)
            } else if (it == 5) {
                nav_view.setCheckedItem(R.id.nav_5_day_view)

                setGoToEarliestVisibleEventLoadTrigger()
                schedule_view.numberOfVisibleDays = 5
                schedule_view.goToEarliestVisibleEvent(2.0)
                ScheduleViewState.setNumberOfVisibleDays(5)
            }
        }

        nav_view.setNavigationItemSelectedListener navHandler@ {

            val id = it.itemId

            when(id) {
                R.id.nav_day_view -> {
                    setGoToEarliestVisibleEventLoadTrigger()
                    schedule_view.numberOfVisibleDays = 1
                    schedule_view.goToEarliestVisibleEvent(2.0)
                    ScheduleViewState.setNumberOfVisibleDays(1)
                }
                R.id.nav_5_day_view -> {
                    setGoToEarliestVisibleEventLoadTrigger()
                    schedule_view.numberOfVisibleDays = 5
                    schedule_view.goToEarliestVisibleEvent(2.0)
                    ScheduleViewState.setNumberOfVisibleDays(5)

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
                home_app_bar_layout.setExpanded(false, true)
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

            schedule_view.invalidate()
            info("NIBBA invalidated schedule view month: $month")

            //ret.forEach {info("${it.name} : ${it.startTime.getHumanReadableDate(false)}")}
            info("NIBBA Month Change Callback returned (month: $month, size: ${ret.count()})")
            ret


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

        schedule_view.setOnEventClickListener lstnr@ { event, eventRect ->
            val intent =
                    when(event) {
                        is Lesson -> Intent(this, LessonDetailsActivity::class.java)
                        is Event -> Intent(this, EventDetailsActivity::class.java)
                        else -> return@lstnr
                    }

            intent.putExtra("event", event)

            startActivity(intent)
        }

        schedule_view.setEventLongPressListener lstnr@ { event, eventRect ->
            when(event) {
                is Event -> {
                    selector(null,
                            listOf("Delete event"),
                            { dialogInterface, i ->
                                when(i) {
                                    0 -> {
                                        if(event isCreatedBy Auth.user) {
                                            // Only confirm again if the user was the creator of the event
                                            confirmDel(event)
                                        } else {
                                            async(UI) {
                                                delEvent(event)
                                            }
                                        }
                                    }
                                }
                            })

                    true
                }
                else -> return@lstnr false
            }
        }

        schedule_view.hourHeight = Resources.getSystem().displayMetrics.heightPixels / 10

        schedule_view.eventMarginVertical = 1.5.dpToPx().toInt()

        create_event_fab.setOnClickListener {
            // TODO: Change this to startActivityForResult
            startActivity<EventCreateUpdateActivity>("mode" to "create")
        }

        applicationContext.registerReceiver(
                RefreshReceiver { intent ->

                    if (intent != null) {
                        val event = intent.getParcelableExtra<Event>("event")
                        longSnackbar(schedule_view, "Event deleted",
                                "UNDO",
                                {
                                    FrontendInterface.createEvent(event)
                                    schedule_view.notifyDatasetChanged()
                                })
                    }

                    schedule_view.notifyDatasetChanged()
                },
                IntentFilter(com.spcore.activities.BROADCAST_DELETED_EVENT))

    }

    override fun onResume() {
        super.onResume()

        info("onResume")

        // TODO: Only notifyDatasetChanged if onActivityResult yields information that a new Event was created
        // this is really really inefficient, but it works for now.
        schedule_view.notifyDatasetChanged()

        ScheduleViewState.getDateAndClear()?.let {
            setScheduleViewDate(it) {
                schedule_view.goToHour(it.getTimeAsDuration().uncarriedHours - 2)
            }
        } ?: run {
            setScheduleViewDate(Calendar.getInstance())
        }
    }

    private fun cueLoadSchedule(year: Int, month: Int) {
        async(UI) {
            val asyncSchedule = bg {
                val x = FrontendInterface.getSchedule(Auth.user.adminNo, year, month)
                info("NIBBA bg asyncSchedule loaded month: $month")
                x
            }

            schedule.removeAll {
                it.startTime isFrom newCalendar(year, month - 1, 1) to
                                    newCalendar(year, month - 1, 1).apply {
                                        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                                    } + (Duration(days = 1) - Duration(millis = 0.1))
            }

            val scheduleToAdd = asyncSchedule.await()
            schedule.addAll(scheduleToAdd)

            info("NIBBA asyncSchedule added month: $month")

            Thread.sleep(42)

            schedule_view.notifyDatasetChanged()
            info("NIBBA notifyDatasetChanged month: $month")
        }
    }

    private fun delEvent(event: Event) {
        FrontendInterface.deleteEvent(event)

        schedule_view.notifyDatasetChanged()

        longSnackbar(schedule_view, "Event deleted",
                "UNDO",
                {
                    FrontendInterface.createEvent(event)
                    schedule_view.notifyDatasetChanged()
                })
    }

    private fun confirmDel(event: Event) {
        alert {
            title = "Are you sure?"
            message = "Deleting this event will also delete it for others who were invited"

            positiveButton("YES", {
                async(UI) {
                    delEvent(event)
                }
            })
        }.show()
    }

    /**
     * Method body is self-explanatory
     */
    private fun setGoToEarliestVisibleEventLoadTrigger() {
        debug("event loaded listener cued")
        schedule_view.setOneshotEventsLoadedListener {
            // once-off event
            schedule_view.goToEarliestVisibleEvent(2.0)

            debug("event loaded listener triggered")
        }
    }

    private fun setScheduleViewDate(cal: Calendar, onLoadedOneshot: (() -> Unit)? = null) {
        if (onLoadedOneshot == null)
            setGoToEarliestVisibleEventLoadTrigger()
        else
            schedule_view.setOneshotEventsLoadedListener { onLoadedOneshot() }

        // Cloning is necessary as the WeekView will set the time to 00:00 upon
        // goToDate. This caused the original Calendar object to die
        schedule_view.goToDate(cal.clone() as Calendar)
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

        async(UI) {
            val asyncSched = bg { Auth.user.getThisMonthSchedule() }
            menu.findItem(R.id.action_home_ats).isVisible =
                    asyncSched.await().getCurrentATSKeyableLessons() != null
        }
        return true
    }

    /**
     * Event handler when action bar items are clicked
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_home_ats -> {
                async(UI) {
                    val cal = Calendar.getInstance()
                    val asyncSched =
                            bg {
                                Auth.user.getSchedule(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
                            }

                    val ATSKeyables = asyncSched.await().getCurrentATSKeyableLessons() ?: return@async

                    val mostRecent = ATSKeyables.last()

                    startActivity<LessonDetailsActivity>(
                            "event" to mostRecent,
                            "open ats dialog" to true,
                            "dismiss notification" to true
                    )
                }

                true
            }
            R.id.action_home_refresh -> {
                async(UI) {
                    bg {
                        SPCoreLocalDB.lessonDAO().clear()
                        SPCoreLocalDB.lessonCacheStatusDAO().clear()
                    }.await()

                    monthsLoadingOrLoaded.clear()
                    schedule_view.notifyDatasetChanged()

                    // For debug purposes only >>> resets ATS submission status when refresh is clicked
                    // ATS.reset()
                }
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }

    }

    class RefreshReceiver(val cb: (Intent?) -> Unit) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            cb(intent)
        }
    }
}
