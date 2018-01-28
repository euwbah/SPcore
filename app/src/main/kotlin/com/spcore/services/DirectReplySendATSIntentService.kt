package com.spcore.services

import android.app.IntentService
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.util.Log
import com.spcore.helpers.NID_ATS_FAILURE
import com.spcore.helpers.NID_LESSON_PROMPT
import com.spcore.helpers.Notifications
import com.spcore.models.Lesson

const val ATS_ACTION_INLINE = "com.spcore.inline.ATS_ACTION"

/** Key to use when putting inline-reply response into Intent extras */
internal const val K_IR_ATS = "com.spcore.extra.IR_ATS"

class DirectReplySendATSIntentService : IntentService("DirectReplySendATSIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        val intent = intent ?: return

        Log.d("Direct Reply", "onReceive")
        val ats = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(K_IR_ATS)
        val lesson = intent.extras.getParcelable<Lesson>(K_PARAM_LESSON)
        if(ats != null)
            submitAts(applicationContext, ats.toString(), lesson)
        else
            Log.e("SPCORE", "UNEXPECTED IGNORED ERROR: ATS inline-reply results bundle was null")

        Notifications.cancelNotification(NID_ATS_FAILURE, NID_LESSON_PROMPT)
    }

    private fun submitAts(context: Context, ats: String, lesson: Lesson) {
        SendATSIntentService.startNew(context, lesson, ats)
    }

    companion object {
        fun newIntent(context: Context, lesson: Lesson) : Intent {
            return Intent(context, DirectReplySendATSIntentService::class.java)
                    .apply {
                        action = ATS_ACTION_INLINE
                        putExtra(K_PARAM_LESSON, lesson)
                        // Note: no need for ATS code extra because it will be taken from the direct reply itself
                    }
        }
    }
}
