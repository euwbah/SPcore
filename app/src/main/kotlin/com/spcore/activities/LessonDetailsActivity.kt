package com.spcore.activities

import android.app.DialogFragment
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.spcore.R
import com.spcore.fragments.ATSEntryDialogFragment
import com.spcore.helpers.*
import com.spcore.models.Lesson

import kotlinx.android.synthetic.main.activity_lesson_details.*
import kotlinx.android.synthetic.main.content_lesson_details.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import kotlin.concurrent.thread

class LessonDetailsActivity : AppCompatActivity(), ATSEntryDialogFragment.OnATSEntryListener {
    private lateinit var lesson: Lesson

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lesson_details)

        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out)

        setSupportActionBar(lesson_details_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        lesson = intent.extras.getParcelable("event")

        lesson_details_toolbar_title.text = "[${lesson.moduleCode}] ${lesson.name}"

        val _24dp = 24.dpToPx()
        async {
            val d = getDrawable(R.drawable.ats).resizeImage(_24dp, _24dp, resources)
            d.setColorFilter(0xcc333333.toInt(), PorterDuff.Mode.MULTIPLY)
            lesson_details_ats_status.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null)
        }

        lesson_details_time_text.text = humanReadableTimeRange(lesson.startTime, lesson.endTime)

        lesson_details_location_text.text = lesson.location

        if (lesson.isATSKeyableNow()) {
            lesson_details_ats_status.let {
                it.visibility = View.VISIBLE

                if (ATS.checkATSSubmitted(lesson)) {
                    it.text = "Submitted"
                    it.setTextColor(0xFF_11_EE_33.toInt())
                } else {
                    it.text = "Not Submitted"
                    it.setTextColor(0xFF_EE_33_11.toInt())
                }
            }

            key_ats_fab.visibility = View.VISIBLE
        } else {
            lesson_details_ats_status.visibility = View.GONE
            key_ats_fab.visibility = View.GONE
        }

        key_ats_fab.setOnClickListener { view ->
            val atsDialog = ATSEntryDialogFragment.newInstance("aaaa", "bbbb")
            atsDialog.show(supportFragmentManager, "key ats")
        }
    }

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
