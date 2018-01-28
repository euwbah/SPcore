package com.spcore.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.firebase.iid.FirebaseInstanceId
import com.spcore.helpers.*
import com.spcore.R
import com.spcore.apis.FrontendInterface
import com.spcore.persistence.initLocalDB
import com.spcore.spmobileapi.SPMobileAPI
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info
import org.jetbrains.anko.uiThread


class SplashActivity : AppStateTrackerActivity("SplashActivity"),
                       AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        initializeNotifications(this.applicationContext)

        doAsync {
            val start = System.currentTimeMillis()

            initSharedPrefs()
            SPMobileAPI.inititialize(getSharedPreferences(getString(R.string.cookie_storage_shared_preference_id), Context.MODE_PRIVATE))

            initLocalDB(applicationContext)

            val registrationToken = FirebaseInstanceId.getInstance().token

            info("registration token: $registrationToken")

            val jwt = Auth.getJwtToken()

            val activityClass =
                    let {
                        if (jwt != null) {
                            if (isOnline(this@SplashActivity)) {

                                // If online, attempt to login every time the app is
                                // started

                                val (adminNo, password) = Auth.getCredentials()
                                val res = FrontendInterface.performLogin(adminNo, password)

                                when {
                                    res !is LoginActivity.LoginStatus.SUCCESS -> LoginActivity::class.java

                                    FrontendInterface.isUserInitializedOnServer() -> {
                                        HardcodedStuff.initialize(Auth.user)
                                        info("Splash auto log-in success")
                                        HomeActivity::class.java
                                    }

                                    else -> InitialLoginActivity::class.java
                                }

                            } else {
                                // Otherwise just base auth on previous state of the app (as per ShPrefs)
                                if (Auth.getUserInitializedLocally()) {
                                    HardcodedStuff.initialize(Auth.user)
                                    HomeActivity::class.java
                                } else
                                    InitialLoginActivity::class.java
                            }
                        } else
                            LoginActivity::class.java
                    }

            // The splash screen should show for a minumum of 500ms
            val msSleep = (200 - (System.currentTimeMillis() - start)).let {
                if (it < 0) 0
                else it
            }

            Thread.sleep(msSleep)

            uiThread {
                this@SplashActivity.startActivity(Intent(this@SplashActivity, activityClass))
            }
        }
    }
}
