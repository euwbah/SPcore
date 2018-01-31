package com.spcore.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.spcore.apis.LessonResponse
import com.spcore.exceptions.WTF
import com.spcore.helpers.Notifications
import com.squareup.moshi.Moshi
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.AnkoLogger
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
                        val json = it["lesson"] ?: run {
                            warn("no lesson key in data")
                            return@async
                        }

                        val moshi = Moshi.Builder().build()
                        val lessonResponseAdapter = moshi.adapter(LessonResponse::class.java)
                        val lessonResponse =
                                lessonResponseAdapter.fromJson(json) ?:
                                throw WTF("Unable to parse lesson response from server notification:\n$json")

                        Notifications.notifyPromptToSubmitATS(lessonResponse.toLesson())
                    }
                }
                else ->
                    warn("Unsupported firebase message data type")
            }
        } ?:
            warn("High-level non-data notifications not supported")
    }
}
