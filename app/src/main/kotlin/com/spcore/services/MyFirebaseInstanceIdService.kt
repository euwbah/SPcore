package com.spcore.services

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.spcore.apis.FrontendInterface
import com.spcore.helpers.Auth
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class MyFirebaseInstanceIdService : FirebaseInstanceIdService(),
                                    AnkoLogger {
    override fun onTokenRefresh() {
        super.onTokenRefresh()

        val newToken = FirebaseInstanceId.getInstance().token

        info("new token: $newToken")

        // Remember to update the server-side by performing a log-in operation
        // (This assumes the server actually updates the token on a per-log-in-request
        //  basis)
        if (Auth.isLoggedIn()) {
            val (adminNo, pass) = Auth.getCredentials()
            FrontendInterface.performLogin(adminNo, pass)
        }
    }
}