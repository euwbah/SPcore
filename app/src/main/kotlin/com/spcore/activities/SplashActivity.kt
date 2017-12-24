package com.spcore.activities

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.spcore.helpers.*
import com.spcore.R
import com.spcore.helpers.SPLASH_SCREEN_MIN_DUR
import kotlinx.coroutines.experimental.*


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val activityClass =
                async {
                    initSharedPrefs()

                    if (Auth.getJwtToken() != null)
                        HomeActivity::class.java
                    else
                        LoginActivity::class.java
                }

        // If JWT token exists, assume user logged in already
        // (although a check will still need to run in the background once the main intent is
        //  opened to ensure that it is not a fake token)

        Handler().postDelayed({
            runBlocking {
                this@SplashActivity.startActivity(Intent(this@SplashActivity, activityClass.await()))
            }
        }, SPLASH_SCREEN_MIN_DUR)
    }

}
