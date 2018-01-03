package com.spcore.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.View
import com.spcore.R
import com.spcore.fragments.ATSEntryDialogFragment
import com.spcore.helpers.*
import com.spcore.models.Lesson
import com.spcore.spmobileapi.ATSResult

import kotlinx.android.synthetic.main.activity_lesson_details.*
import kotlinx.android.synthetic.main.content_lesson_details.*
import kotlinx.coroutines.experimental.async

private const val ATS_SUBMITTED_COLOR = 0xFF_11_EE_33.toInt()
private const val ATS_NOT_SUBMITTED_COLOR = 0xFF_EE_33_11.toInt()

/**
 * Keys for extras bundle:
 *  - `event`: The parcelled [Lesson] object
 *  - `open ats dialog` (optional): A [Boolean] - whether or not to show the ATS dialog automatically
 *  - `dismiss notification` (optional): An [Int] representing notification ID to dismiss
 */
class LessonDetailsActivity : AppStateTrackerActivity("LessonDetailsActivity"),
                              ATSEntryDialogFragment.ATSDialogEventsHandler {
    private lateinit var lesson: Lesson

    private var atsDialogFragment: ATSEntryDialogFragment? = null

    /** replacement for [key_ats_fab.isEnabled]/[key_ats_fab.setEnabled] as it causes some
     * z-index glitching */
    private var fabEnabled = true
    set(enabled) {
        key_ats_fab.alpha =
                if(enabled)
                    1f
                else
                    0.3f

        field = enabled
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lesson_details)

        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out)

        setSupportActionBar(lesson_details_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        /*
            READ EXTRAS
         */

        lesson = intent.extras.getParcelable("event")
        val openDialog = intent.extras.getBoolean("open ats dialog", false)
        val dismissNotifID = intent.extras.getInt("dismiss notification", -1337)

        lesson_details_toolbar_title.text = "[${lesson.moduleCode}] ${lesson.name}"

        val _24dp = 24f.dpToPx()
        Handler(mainLooper).post {
            val d = getDrawable(R.drawable.ats).resizeImage(_24dp, _24dp, resources)
            d.setColorFilter(0xcc333333.toInt(), PorterDuff.Mode.MULTIPLY)
            lesson_details_ats_status.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null)
        }

        lesson_details_time_text.text = humanReadableTimeRange(lesson.startTime, lesson.endTime)

        lesson_details_location_text.text = lesson.location

        key_ats_fab.visibility = View.GONE

        if (lesson.isATSKeyableNow()) {
            lesson_details_ats_status.let {
                it.visibility = View.VISIBLE

                if (ATS.checkATSSubmitted(lesson)) {
                    it.text = "Submitted"
                    it.setTextColor(ATS_SUBMITTED_COLOR)
                } else {
                    it.text = "Not Submitted"
                    it.setTextColor(ATS_NOT_SUBMITTED_COLOR)
                    key_ats_fab.visibility = View.VISIBLE
                }
            }
        } else {
            lesson_details_ats_status.visibility = View.GONE
        }

        key_ats_fab.setOnClickListener { view ->
            if(fabEnabled) {
                atsDialogFragment = ATSEntryDialogFragment.newInstance(lesson, "")
                atsDialogFragment?.show(supportFragmentManager, "key ats")
            }
        }

        ATSSubmissionResultReceiver().activateReceiver()

        if(openDialog) {
            atsDialogFragment = ATSEntryDialogFragment.newInstance(lesson, intent.extras.getString("errmsg", ""))
            atsDialogFragment?.show(supportFragmentManager, "key ats")
        }

        if(dismissNotifID != -1337) {
            Notifications.cancelNotification(dismissNotifID)
        }
    }

    /**
     * This is invoked when the ATS code submitted passes client-side validation
     * in the [ATSEntryDialogFragment]
     */
    override fun onSuccessfulRequest() {
        atsDialogFragment?.dismiss()

        // Temporarily deactivate the FAB while the submission requests are in progress
        fabEnabled = false
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

                    lesson_details_ats_status.apply {
                        text = "Submitted"
                        setTextColor(ATS_SUBMITTED_COLOR)
                    }

                    key_ats_fab.visibility = View.GONE
                }

                BROADCAST_ATS_FAILURE -> {
                    val errmsg =
                            (intent.getSerializableExtra("error") as ATSResult.Errors._Serializable)
                                    .deserialize().toString()

                    if(atsDialogFragment?.dialog?.isShowing == true)
                        atsDialogFragment?.errmsg = errmsg
                    else {
                        Handler(mainLooper).post {
                            atsDialogFragment = ATSEntryDialogFragment.newInstance(lesson, errmsg)
                            atsDialogFragment?.show(supportFragmentManager, "key ats")
                        }
                    }

                    // Re-activate the fab because the user needs to key it again
                    fabEnabled = true
                }
            }
        }
    }

}
