package com.spcore.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.spcore.R
import com.spcore.adapters.UserProfileListAdapter
import com.spcore.helpers.*
import com.spcore.helpers.arListUsers
import com.spcore.models.User
import kotlinx.android.synthetic.main.activity_invitations.*
import org.jetbrains.anko.sdk25.coroutines.onClick


class InvitationActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invitations)

        val arrayListOfSearchedUsers = ArrayList<User>()
        invitation_lv.adapter = UserProfileListAdapter(this@InvitationActivity, arrayListOfSearchedUsers)
        done.onClick {
            performSearh(arrayListOfSearchedUsers)
        }
        event_crud_cancel_button.onClick {
            finish()
        }
    }

    private fun performSearh(arrayList: ArrayList<User>){
        // dummy data
        arListUsers.add(NatoshiSakamoto)
        arListUsers.add(NatoshiSakamoto1)
        arListUsers.add(NatoshiSakamoto2)
        arListUsers.add(NatoshiSakamoto3)

        if(userSearch.text.isNullOrBlank())
            finish()
        else {
            val searchedUsers = arListUsers.find { it.username == userSearch.textStr }
            searchedUsers?.let { arrayList.add(it) }
            invitation_lv.invalidateViews()
        }

    }


}