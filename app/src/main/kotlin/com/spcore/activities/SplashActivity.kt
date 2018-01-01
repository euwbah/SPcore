package com.spcore.activities

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.spcore.helpers.*
import com.spcore.R
import com.spcore.apis.FrontendInterface
import com.spcore.helpers.SPLASH_SCREEN_MIN_DUR
import kotlinx.coroutines.experimental.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import kotlin.system.measureTimeMillis


class SplashActivity : AppStateTrackerActivity("SplashActivity") {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        initializeNotifications(this.applicationContext)

        doAsync {
            val start = System.currentTimeMillis()

            initSharedPrefs()

            val jwt = Auth.getJwtToken()

            val activityClass =
                    if (jwt != null) {
                        if(FrontendInterface.isUserInitializedOnServer())
                            HomeActivity::class.java
                        else
                            InitialLogin::class.java
                    }
                    else
                        LoginActivity::class.java

            // The splash screen should show for a minumum of 500ms
            val msSleep = (500 - (System.currentTimeMillis() - start)).let {
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
