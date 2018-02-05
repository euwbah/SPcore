package com.spcore.services

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.widget.Toast
import com.spcore.helpers.*
import com.spcore.models.Lesson
import com.spcore.spmobileapi.ATSResult
import com.spcore.spmobileapi.Result
import com.spcore.spmobileapi.SPMobileAPI

/**
 * Key to activate ATS submission function
 */
const val K_ACTION_SUBMIT_ATS = "com.spcore.action.SUBMIT_ATS"

    /** Param key of the ATS code [String] */
    const val K_PARAM_ATS_CODE = "com.spcore.extra.ATS_CODE"
    /** Param key of the [Lesson] the ATS submission was for */
    const val K_PARAM_LESSON = "com.spcore.extra.LESSON_ID"

/**
 * [IntentService] for submitting ATS
 */
class SendATSIntentService : IntentService("SendATSIntentService") {

    override fun onHandleIntent(intent: Intent) {
        when(intent.action) {
            K_ACTION_SUBMIT_ATS -> {
                val ats = intent.extras.getString(K_PARAM_ATS_CODE)
                val lesson = intent.extras.getParcelable<Lesson>(K_PARAM_LESSON)
                submitAts(ats, lesson)
            }
        }
    }

    private fun submitAts(ats: String, lesson: Lesson) {
        val (adminNo, pass) = Auth.getCredentials()

        // Simulate server access
        Thread.sleep(2000)

        val atsResult =
//                if(HARDCODE_MODE) {
//                    when (ats) {
//                        "111111" ->
//                            Result.Error<Nothing?, ATSResult.Errors>(ATSResult.Errors.INVALID_CODE)
//                        "222222" ->
//                            Result.Error<Nothing?, ATSResult.Errors>(ATSResult.Errors.NO_INTERNET)
//                        "333333" ->
//                            Result.Error<Nothing?, ATSResult.Errors>(ATSResult.Errors.INVALID_CREDENTIALS)
//                        "444444" ->
//                            Result.Error<Nothing?, ATSResult.Errors>(ATSResult.Errors.NOT_CONNECTED_TO_SCHOOL_WIFI)
//                        "555555" ->
//                            Result.Error<Nothing?, ATSResult.Errors>(ATSResult.Errors.ALREADY_ENTERED)
//                        "666666" ->
//                            Result.Error<Nothing?, ATSResult.Errors>(ATSResult.Errors.WRONG_CLASS("YE/MUMS/CLASS"))
//                        else ->
//                            Result.Ok(null)
//                    }
//                } else
                    SPMobileAPI.sendATS(adminNo, pass, ats)


        Log.d("APP STATE", AppState.getForegroundActivity())

        when(atsResult) {
            is Result.Ok -> {

                ATS.markATSSubmitted(lesson)

                when {
                    AppState foregroundIs "LessonDetailsActivity" -> {
                        val broadcast = Intent(BROADCAST_ATS_SUCCESS)
                        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
                        Handler(mainLooper).post({
                            Toast
                                    .makeText(this@SendATSIntentService, "ATS Submitted Successfully", Toast.LENGTH_SHORT)
                                    .show()
                        })
                    }
                    AppState foregroundIsnt "none" -> // If app is open, show toast message
                        Handler(mainLooper).post({
                            Toast
                                    .makeText(this@SendATSIntentService, "ATS Submitted Successfully", Toast.LENGTH_SHORT)
                                    .show()
                        })
                    else -> Notifications.notifyATSSuccess()
                }
            }

            is Result.Error ->
                when {
                    AppState foregroundIs "LessonDetailsActivity" -> {
                        val broadcast = Intent(BROADCAST_ATS_FAILURE)
                                .putExtra("error", atsResult.errorValue.toSerializable())
                        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
                    }
                    AppState foregroundIsnt "none" -> {
                        val broadcast =
                                Intent(BROADCAST_CREATE_SNACKBAR)
                                        .putExtra("type", "ats error")
                                        .putExtra("errmsg", atsResult.errorValue.toString())

                        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
                    }
                    else -> Notifications.notifyATSError(
                            lesson,
                            atsResult.errorValue.toString(),

                            // whether to makeSticky or not
                            when(atsResult.errorValue) {
                                ATSResult.Errors.INVALID_CODE,
                                is ATSResult.Errors.WRONG_CLASS -> true

                                else -> false
                            },

                            // whether to show direct-reply
                            when(atsResult.errorValue) {
                                ATSResult.Errors.INVALID_CODE,
                                is ATSResult.Errors.WRONG_CLASS ->
                                    true
                                else ->
                                    false
                            }
                    )
                }
        }
    }

    companion object {
        /**
         * Starts this service to perform action Foo with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        @JvmStatic
        fun startNew(context: Context, lesson: Lesson, atsCode: String) {
            val intent = Intent(context, SendATSIntentService::class.java).apply {
                action = K_ACTION_SUBMIT_ATS
                putExtra(K_PARAM_ATS_CODE, atsCode)
                putExtra(K_PARAM_LESSON, lesson)
            }
            context.startService(intent)
        }

        /**
         * @param atsCode If this is not provided, function assumes that ATS code needs to be
         *                extracted from the inline-reply Bundle
         */
        fun newIntent(context: Context, lesson: Lesson, atsCode: String): Intent {
            Log.d("NEW INTENT", "SendATSIntentService")
            return Intent(context, SendATSIntentService::class.java).apply {

                putExtra(K_PARAM_LESSON, lesson)
                putExtra(K_PARAM_ATS_CODE, atsCode)

                action = K_ACTION_SUBMIT_ATS
            }
        }
    }
}
