package com.spcore.services.intents

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.spcore.helpers.AppState
import com.spcore.helpers.Auth
import com.spcore.helpers.BROADCAST_ATS_FAILURE
import com.spcore.helpers.BROADCAST_ATS_SUCCESS
import com.spcore.spmobileapi.Result
import com.spcore.spmobileapi.SPMobileAPI

/**
 * Key to activate ATS submission function
 */
private const val K_ACTION_SUBMIT_ATS = "com.spcore.action.SUBMIT_ATS"
    /**
     * Param key containing the ATS code in the intent extras bundle
     */
    private const val K_PARAM_ATS_CODE = "com.spcore.extra.ATS_CODE"

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
class SendATSIntentService : IntentService("SendATSIntentService") {

    override fun onHandleIntent(intent: Intent) {
        when(intent.action) {
            K_ACTION_SUBMIT_ATS -> {
                val ats = intent.extras.getString(K_PARAM_ATS_CODE)
                submitAts(ats)
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
                        val notifManager = systemService(Context.NOTIFICATION_SERVICE)
                    }

            is Result.Error ->
                    if(AppState.foregroundActivityIs("LessonDetailsActivity")) {
                        val broadcast = Intent(BROADCAST_ATS_FAILURE)
                                .putExtra("error", atsResult.errorValue.toSerializable())
                        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
                    } else {

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
    }
}
