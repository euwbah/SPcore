package com.spcore.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.spcore.R
import com.spcore.adapters.UserProfileListAdapter
import com.spcore.helpers.*
import com.spcore.helpers.arListUsers
import kotlinx.android.synthetic.main.activity_invitations.*
import org.jetbrains.anko.sdk25.coroutines.onClick


class InvitationActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invitations)

        done.onClick {
            performSearh()
        }
    }

    private fun performSearh(){
        arListUsers.add(NatoshiSakamoto)
        arListUsers.add(NatoshiSakamoto1)
        arListUsers.add(NatoshiSakamoto2)
        arListUsers.add(NatoshiSakamoto3)


        if(userSearch.text == null)
            finish()
        else {
            arListUsers.forEach {
                if(it.username == userSearch.text.toString()){
                    invitation_lv.adapter = UserProfileListAdapter(this@InvitationActivity, arListUsers)
                }
            }

        }

    }


}