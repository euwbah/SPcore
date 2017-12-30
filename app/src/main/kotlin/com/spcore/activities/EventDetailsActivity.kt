package com.spcore.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.spcore.R
import kotlinx.android.synthetic.main.activity_event_details.*

class EventDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)
        setSupportActionBar(event_details_toolbar)


    }
}
