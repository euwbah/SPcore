package com.spcore.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import com.spcore.R
import com.spcore.fragments.ATSEntryDialogFragment
import com.spcore.helpers.*
import com.spcore.models.Lesson
import com.spcore.spmobileapi.ATSResult

import kotlinx.android.synthetic.main.activity_lesson_details.*
import kotlinx.android.synthetic.main.content_lesson_details.*
import kotlinx.coroutines.experimental.async

class LessonDetailsActivity : AppStateTrackerActivity("LessonDetailsActivity") {
    private lateinit var lesson: Lesson

    private var atsDialogFragment: ATSEntryDialogFragment? = null

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
            atsDialogFragment = ATSEntryDialogFragment.newInstance("")
            atsDialogFragment?.show(supportFragmentManager, "key ats")
        }

        ATSSubmissionResultReceiver().activateReceiver()

        if(intent.extras.getBoolean("open ats dialog", false)) {
            atsDialogFragment = ATSEntryDialogFragment.newInstance(intent.extras.getString("errmsg", ""))
            atsDialogFragment?.show(supportFragmentManager, "key ats")
        }
    }

    /**
     * Usage: instantiate the Receiver and call [activateReceiver]
     */
    inner class ATSSubmissionResultReceiver : BroadcastReceiver() {

        fun activateReceiver() {
            arrayOf(BROADCAST_ATS_SUCCESS, BROADCAST_ATS_FAILURE)
                    .map {
                        LocalBroadcastManager
                                .getInstance(this@LessonDetailsActivity)
                                .registerReceiver(
                                        this,
                                        IntentFilter(it)
                                )
                    }
        }

        /**
         * Action to take upon receiving broadcast.
         *
         * REMINDER: Broadcasts are only sent if the LessonDetailsActivity is active
         */
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                BROADCAST_ATS_SUCCESS -> {
                    if(atsDialogFragment?.dialog?.isShowing == true)
                        atsDialogFragment?.dismiss()
                }

                BROADCAST_ATS_FAILURE -> {
                    val errmsg =
                            (intent.getSerializableExtra("error") as ATSResult.Errors._Serializable)
                                    .deserialize().toString()

                    if(atsDialogFragment?.dialog?.isShowing == true)
                        atsDialogFragment?.errmsg = errmsg
                    else {
                        atsDialogFragment = ATSEntryDialogFragment.newInstance(errmsg)
                    }
                }
            }
        }
    }

}
