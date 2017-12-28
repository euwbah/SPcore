package com.spcore.broadcasts

import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.spcore.helpers.K_IR_ATS
import com.spcore.models.Lesson
import com.spcore.services.K_PARAM_LESSON
import com.spcore.services.SendATSIntentService

const val ATS_ACTION_INLINE = "com.spcore.inline.ATS_ACTION"

class DirectReplyATSBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("Direct Reply", "onReceive")
        val ats = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(K_IR_ATS)
        val lesson = intent.extras.getParcelable<Lesson>(K_PARAM_LESSON)
        if(ats != null)
            submitAts(context, ats.toString(), lesson)
        else
            Log.e("SPCORE", "UNEXPECTED IGNORED ERROR: ATS inline-reply results bundle was null")
    }

    private fun submitAts(context: Context, ats: String, lesson: Lesson) {
        SendATSIntentService.startNew(context, lesson, ats)
    }

    companion object {
        fun newIntent(context: Context, lesson: Lesson) : Intent {
            return Intent(context, DirectReplyATSBroadcastReceiver::class.java)
                    .apply {
                        action = ATS_ACTION_INLINE
                        putExtra(K_PARAM_LESSON, lesson)
                        // Note: no need for ATS code extra because it will be taken from the direct reply itself
                    }
        }
    }
}