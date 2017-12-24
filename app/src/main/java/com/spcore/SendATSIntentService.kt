package com.spcore

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.spcore.helpers.AppState
import com.spcore.helpers.Auth
import com.spcore.helpers.BROADCAST_ATS_SUCCESS
import com.spcore.spmobileapi.Result
import com.spcore.spmobileapi.SPMobileAPI

private const val K_ACTION_SUBMIT_ATS = "com.spcore.action.SUBMIT_ATS"

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
            K_ACTION_SUBMIT_ATS ->
                    intent.extras.getInt(K_PARAM_ATS_CODE)
        }
    }

    private fun submitAts(ats: Int) {
        val (adminNo, pass) = Auth.getCredentials()

        val atsResult = SPMobileAPI.sendATS(adminNo, pass, ats)

        Log.d("APP STATE", AppState.getForegroundActivity())

        when(atsResult) {
            is Result.Ok ->
                    if(AppState.getForegroundActivity() == "LessonDetailsActivity") {
                        val broadcast = Intent(BROADCAST_ATS_SUCCESS)
                        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
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
        fun startActionFoo(context: Context, param1: Int) {
            val intent = Intent(context, SendATSIntentService::class.java).apply {
                action = K_ACTION_SUBMIT_ATS
                putExtra(K_PARAM_ATS_CODE, param1)
            }
            context.startService(intent)
        }
    }
}
