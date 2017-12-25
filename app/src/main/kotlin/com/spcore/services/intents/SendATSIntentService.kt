package com.spcore.services.intents

import android.app.IntentService
import android.app.RemoteInput
import android.content.Intent
import android.content.Context
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.spcore.helpers.*
import com.spcore.spmobileapi.ATSResult
import com.spcore.spmobileapi.Result
import com.spcore.spmobileapi.SPMobileAPI

/**
 * Key to activate ATS submission function
 */
const val K_ACTION_SUBMIT_ATS = "com.spcore.action.SUBMIT_ATS"
const val K_ACTION_SUBMIT_ATS_INLINE = "com.spcore.action.SUBMIT_ATS_INLINE"
    /**
     * Param key containing the ATS code in the intent extras bundle
     */
    const val K_PARAM_ATS_CODE = "com.spcore.extra.ATS_CODE"

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
class SendATSIntentService() : IntentService("SendATSIntentService") {

    override fun onHandleIntent(intent: Intent) {
        when(intent.action) {
            K_ACTION_SUBMIT_ATS -> {
                val ats = intent.extras.getString(K_PARAM_ATS_CODE)
                submitAts(ats)
            }
            K_ACTION_SUBMIT_ATS_INLINE -> {
                val ats = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(K_IR_ATS)
                if(ats != null)
                    submitAts(ats.toString())
                else
                    Log.e("SPCORE", "UNEXPECTED IGNORED ERROR: ATS inline-reply results bundle was null")
            }
        }
    }

    private fun submitAts(ats: String) {
        val (adminNo, pass) = Auth.getCredentials()

        val atsResult = SPMobileAPI.sendATS(adminNo, pass, ats)

        Log.d("APP STATE", AppState.getForegroundActivity())

        when(atsResult) {
            is Result.Ok ->
                    if(AppState.foregroundActivityIs("LessonDetailsActivity")) {
                        val broadcast = Intent(BROADCAST_ATS_SUCCESS)
                        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
                    } else {

                    }

            is Result.Error ->
                    if(AppState.foregroundActivityIs("LessonDetailsActivity")) {
                        val broadcast = Intent(BROADCAST_ATS_FAILURE)
                                .putExtra("error", atsResult.errorValue.toSerializable())
                        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
                    } else {
                        Notifications.notifyATSError(
                                when(atsResult.errorValue) {
                                    ATSResult.Errors.INVALID_CODE -> "Invalid code was entered, enter again."
                                    ATSResult.Errors.ALREADY_ENTERED -> "ATS code was already submitted"
                                    is ATSResult.Errors.WRONG_CLASS -> {
                                        val `class` = (atsResult.errorValue as ATSResult.Errors.WRONG_CLASS).wrongClass
                                        "ATS code was for class $`class`"
                                    }
                                    ATSResult.Errors.NO_INTERNET -> "No internet connection"
                                    ATSResult.Errors.NOT_CONNECTED_TO_SCHOOL_WIFI -> "Not connected to SPStudent"
                                    ATSResult.Errors.INVALID_CREDENTIALS -> "SPice account password was just changed, log out and log in again"
                                },

                                atsResult.errorValue !is ATSResult.Errors.WRONG_CLASS,

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
        // TODO: Customize helper method
        @JvmStatic
        fun startNew(context: Context, atsCode: String) {
            val intent = Intent(context, SendATSIntentService::class.java).apply {
                action = K_ACTION_SUBMIT_ATS
                putExtra(K_PARAM_ATS_CODE, atsCode)
            }
            context.startService(intent)
        }

        /**
         * @param atsCode If this is not provided, function assumes that ATS code needs to be
         *                extracted from the inline-reply Bundle
         */
        fun newIntent(context: Context, atsCode: String? = null): Intent {
            return Intent(context, SendATSIntentService::class.java).apply {
                action =
                        if(atsCode != null) {
                            putExtra(K_PARAM_ATS_CODE, atsCode)
                            K_ACTION_SUBMIT_ATS
                        } else
                            K_ACTION_SUBMIT_ATS_INLINE
            }
        }
    }
}
