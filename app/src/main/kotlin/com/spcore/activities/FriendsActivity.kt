package com.spcore.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.spcore.R
import com.spcore.adapters.UserProfileListAdapter
import com.spcore.helpers.Auth
import kotlinx.android.synthetic.main.activity_friends.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg

class FriendsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        setSupportActionBar(friend_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "Friends"

        async(UI) {
            val asyncFriends = bg {
                Auth.user.getFriends()
            }

            val friends = asyncFriends.await()

            friend_list_view.adapter = UserProfileListAdapter(this@FriendsActivity, friends)
        }
    }
}
