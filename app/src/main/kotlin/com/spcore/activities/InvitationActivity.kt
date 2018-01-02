package com.spcore.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.spcore.R
import com.spcore.adapters.UserProfileListAdapter
import com.spcore.helpers.*
import com.spcore.helpers.arListUsers
import com.spcore.models.User
import kotlinx.android.synthetic.main.activity_invitations.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onEditorAction
import org.jetbrains.anko.sdk25.coroutines.onKey
import org.jetbrains.anko.wrapContent


class InvitationActivity: AppCompatActivity() {

    private val arrayListOfSearchedUsers = ArrayList<User>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invitations)

        invitation_lv.adapter = UserProfileListAdapter(this@InvitationActivity, arrayListOfSearchedUsers)
        userSearch.onEditorAction { _, _, _ -> performSearch((arrayListOfSearchedUsers))}
        invitation_cancel_button.onClick {
            finish()
        }
    }\

    private fun performSearch(arrayList: ArrayList<User>) {
        arrayList.clear()
        if(userSearch.text.isNullOrBlank())
            finish()
        else {
            val searchedUsers = arListUsers.filter {
                it.username.startsWith(userSearch.textStr)
            }.forEach { arrayList.add(it) }

            invitation_lv.apply {
                invalidateViews()
                setHeightToWrapContent()
            }
        }

    }


}