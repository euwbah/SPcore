package com.spcore.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.longToast
import org.jetbrains.anko.warn

class MyFirebaseMessagingService : FirebaseMessagingService(),
                                   AnkoLogger {

    companion object {
        // Notification types
        const val TEST_DATA_MESSAGE = "tdm"
        const val LESSON_NOTIFICATION = "lesson"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.data?.let {
            val type = it["type"]

            when(type) {
                TEST_DATA_MESSAGE -> {
                    async(UI) {
                        longToast(it["message"] ?: "No message")
                    }
                }
                LESSON_NOTIFICATION -> {
                    async(UI) {
                        val json = it["lesson"] ?: "no lesson"
                        info(json)
                        longToast(json)
                    }
                }
                else ->
                    warn("Unsupported firebase message data type")
            }
        } ?:
            warn("High-level non-data notifications not supported")
    }
}
