package com.spcore.helpers

import android.app.*
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.RemoteInput
import com.spcore.R
import com.spcore.activities.LessonDetailsActivity
import com.spcore.services.intents.SendATSIntentService


/*
 * Notification channels & respective display names
 */

/**
 * For notifs related to ATS
 */
internal const val NC_ATS = "com.spcore.notifchannel.ATS"
/** Notification Channel name as displayed in the notification settings */
internal const val NC_ATS_DN = "ATS Prompts"

/*
 * Notification IDs
 */

internal const val NID_ATS_FAILURE = 0
internal const val NID_ATS_SUCCESS = 1

/** Key to use when putting inline-reply response into Intent extras */
internal const val K_IR_ATS = "com.spcore.extra.IR_ATS"

/** Inline reply request code identifier for ATS inline-reply */
internal const val IR_RC_ATS = 0

/**
 * [Notifications]
 *
 * [How to remove sticky notifications](https://stackoverflow.com/questions/19268450/how-to-remove-notification-from-notification-bar-programmatically-in-android)
 */
class CNotifications(context: Context) : ContextWrapper(context) {
    private val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    /**
     * Note: This will be null if not Android Oreo
     */
    private val atsNotifChannel by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(NC_ATS, NC_ATS_DN, NotificationManager.IMPORTANCE_HIGH)
                    .apply {
                        description = "For ATS submission prompts & submission response"
                        enableLights(true)
                        lightColor = Color.RED
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 500, 200, 1000)
                    }
        } else {
            null
        }
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notifManager.createNotificationChannel(atsNotifChannel)

        }
    }

    fun notifyATSSuccess() {
        val notificationBuilder =
                NotificationCompat
                        .Builder(this, NC_ATS)
                        .setSmallIcon(R.drawable.ats) // FIXME: This is stupid
                        .setLargeIcon(getDrawable(R.drawable.ats).toBitmap())
                        .setContentTitle("ATS submitted")

        notifManager.notify(NID_ATS_SUCCESS, notificationBuilder.build())
    }

    /**
     * @param errmsg Error message to display
     * @param makeSticky Set true if notification should be sticky
     * @param inlineReply Set true to allow user to re-enter the ATS code inline
     */
    fun notifyATSError(errmsg: String, makeSticky: Boolean, inlineReply: Boolean) {

        val notificationBuilder =
                NotificationCompat
                        .Builder(this, NC_ATS)
                        .setSmallIcon(R.drawable.ats) // FIXME: This is stupid
                        .setLargeIcon(getDrawable(R.drawable.ats).toBitmap())
                        .setContentTitle("ATS Submission Failed")
                        .setContentText(errmsg)
                        .setOngoing(makeSticky)


        // i.e. the intent which shows up when the user clicks on the notification body
        val intentToSpawn =
                Intent(this, LessonDetailsActivity::class.java)
                        .putExtra("show ats dialog", true) // this is tentative, perhaps not all
                                                                      // cases require this intent with the
                                                                      // ats dialog open

        val stackBuilder = TaskStackBuilder.create(this)
        // addParentStack will read parentActivityName metadata attributes from AndroidManifest.xml
        // and automatically reconstruct the back stack up to the root task
        stackBuilder.addParentStack(LessonDetailsActivity::class.java)
        stackBuilder.addNextIntent(intentToSpawn)

        val spawnedPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        notificationBuilder.setContentIntent(spawnedPendingIntent)


        if (inlineReply) {
            // The inline-reply action restarts the SendATSIntentService
            val inlineReplyIntent =
                    PendingIntent.getBroadcast(
                            applicationContext,
                            IR_RC_ATS,
                            SendATSIntentService.newIntent(this),
                            PendingIntent.FLAG_UPDATE_CURRENT)
            val remoteInput =
                    RemoteInput.Builder(K_IR_ATS)
                            .setLabel("Key ATS...")
                            .build()
            val inlineReplyNotifAction =
                        NotificationCompat.Action.Builder(
                                R.drawable.ats,
                                "Submit ATS",
                                inlineReplyIntent)
                        .addRemoteInput(remoteInput)
                        .build()

            notificationBuilder.addAction(inlineReplyNotifAction)
        }

        notifManager.notify(NID_ATS_FAILURE, notificationBuilder.build())
    }

}

private var isInit = false

/**
 * Remember to call [initializeNotifications] before accessing this!
 */
lateinit var Notifications: CNotifications

fun initializeNotifications(context: Context) {
    Notifications = CNotifications(context)
}