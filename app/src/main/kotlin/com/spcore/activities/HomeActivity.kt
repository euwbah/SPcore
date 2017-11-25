package com.spcore.activities

import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.spcore.R
import com.spcore.helpers.FULL_MONTH_YEAR_DATE_FORMAT
import com.spcore.services.FrontendInterface

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
                R.id.nav_day_view -> TODO()
                R.id.nav_week_view -> TODO()
                // etc...
            }

            // TODO: Only return true if nav selection is to appear highlighted, if not, return false
            return@navHandler true
        }

        toolbar_dropdown_calendar.setLocale(TimeZone.getDefault(), Locale.ENGLISH)
        toolbar_dropdown_calendar.setShouldDrawDaysHeader(true)
        toolbar_dropdown_calendar.setListener(object : CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                setMYTextView(dateClicked)
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                setMYTextView(firstDayOfNewMonth)
            }
        })

        // Set to today
        setCurrentDate(Date())

        run datePickerToggling@ {
            var isExpanded = false
            date_picker_button.setOnClickListener {
                if (isExpanded)
                    ViewCompat.animate(date_picker_arrow).rotation(0f).start()
                else
                    ViewCompat.animate(date_picker_arrow).rotation(-180f).start()

                isExpanded = !isExpanded
                app_bar_layout.setExpanded(isExpanded, true)
            }
        }

        // Note: month here is 1-based
        schedule_view.setMonthChangeListener {
            year, month ->
                Log.d("SPCORE SCHED VIEW", "MonthChange: $year, $month")
                FrontendInterface.getSchedule(year, month).map { it.toWeekViewEvent() }
        }

        schedule_view.setEmptyViewClickListener {  }
        schedule_view.setEmptyViewLongPressListener {  }
        schedule_view.setEventLongPressListener { event, eventRect ->  }
        schedule_view.setOnEventClickListener { event, eventRect ->  }

        schedule_view.setScrollListener {
            newFirstVisibleDay, oldFirstVisibleDay ->
                setCurrentDate(newFirstVisibleDay.time)
        }
    }


    private fun setCurrentDate(date: Date) {
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
