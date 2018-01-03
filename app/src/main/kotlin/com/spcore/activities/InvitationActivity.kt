package com.spcore.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.spcore.R
import com.spcore.adapters.UserProfileListAdapter
import com.spcore.helpers.*
import com.spcore.helpers.HardcodedUsers
import com.spcore.models.User
import kotlinx.android.synthetic.main.activity_invitations.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onEditorAction


class InvitationActivity: AppCompatActivity() {

    private val arrayListOfSearchedUsers = ArrayList<User>()
    private val arrayListOfAddedGuest = ArrayList<User>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invitations)


        userSearch.onEditorAction { _, _, _ ->
            invitation_lv.adapter = UserProfileListAdapter(this@InvitationActivity, arrayListOfSearchedUsers)
            performSearch((arrayListOfSearchedUsers))
        }

        invitation_cancel_button.onClick {
            finish()
        }

        invitation_lv.setOnItemClickListener { _, view, _, _ ->
            arrayListOfAddedGuest.add(view.tag as User)
            invitation_lv.adapter = UserProfileListAdapter(this@InvitationActivity, arrayListOfAddedGuest)
        }
    }

    /**
     * Simple search function that uses startsWith
     * @param arrayList of User added. To be changed in the future to integrate with the database
     */
    private fun performSearch(arrayList: ArrayList<User>) {
        arrayList.clear()
        if(userSearch.text.isNullOrBlank())
            finish()
        else {
            val searchedUsers = HardcodedUsers.filter {
                it.username.startsWith(userSearch.textStr)
            }.forEach { arrayList.add(it) }

            invitation_lv.apply {
                invalidateViews()
                setHeightToWrapContent()
            }
        }

    }



}