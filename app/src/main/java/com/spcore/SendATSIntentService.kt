package com.spcore

import android.app.IntentService
import android.content.Intent
import android.content.Context
import com.spcore.helpers.Auth
import com.spcore.spmobileapi.ATSResult
import com.spcore.spmobileapi.SPMobileAPI

// TODO: Rename actions, choose action names that describe tasks that this
// IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
private const val K_ACTION_SUBMIT_ATS = "com.spcore.action.FOO"

// TODO: Rename parameters
private const val K_PARAM_ATS = "com.spcore.extra.PARAM1"


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
                    intent.extras.getInt(K_PARAM_ATS)
        }
    }

    private fun submitAts(ats: Int) {
        val (adminNo, pass) = Auth.getCredentials()
        SPMobileAPI.sendATS(adminNo, pass, ats).tryGetIfNot {

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
                putExtra(K_PARAM_ATS, param1)
            }
            context.startService(intent)
        }
    }
}
