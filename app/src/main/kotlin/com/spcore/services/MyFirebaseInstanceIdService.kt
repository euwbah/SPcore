package com.spcore.services

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class MyFirebaseInstanceIdService : FirebaseInstanceIdService(),
                                    AnkoLogger {
    override fun onTokenRefresh() {
        super.onTokenRefresh()

        val newToken = FirebaseInstanceId.getInstance().token

        info("new token: $newToken")
    }
}