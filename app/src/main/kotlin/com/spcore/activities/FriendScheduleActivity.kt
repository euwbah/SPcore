package com.spcore.activities

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.spcore.R

import kotlinx.android.synthetic.main.activity_friend_schedule.*

class FriendScheduleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_schedule)
        setSupportActionBar(friend_toolbar)

    }

}
