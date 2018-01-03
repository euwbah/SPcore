package com.spcore.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.EditorInfo
import com.spcore.R
import com.spcore.adapters.UserProfileListAdapter
import com.spcore.helpers.*
import com.spcore.helpers.HardcodedUsers
import com.spcore.models.User
import kotlinx.android.synthetic.main.activity_invitations.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import org.jetbrains.anko.sdk25.coroutines.textChangedListener


class InvitationActivity: AppCompatActivity() {

    private lateinit var userSuggestionsAdapter: UserProfileListAdapter
    private lateinit var invitedGuestsAdapter: UserProfileListAdapter

    private val inviteList = arrayListOf<User>()

    /**
     * False: invitedGuestsAdapter
     * True: userSuggestionsAdapter
     */
    private var searchMode = false
        set(x) {
            if (x) {
                invitation_no_one_text.visibility = View.GONE
                invitation_invited_text.visibility = View.GONE
                invitation_lv.adapter = userSuggestionsAdapter
                invitation_lv.dividerHeight = 1
            } else {
                invitation_no_one_text.visibility =
                        if(inviteList.isEmpty())
                            View.VISIBLE
                        else
                            View.GONE

                invitation_invited_text.visibility = View.VISIBLE
                invitation_lv.adapter = invitedGuestsAdapter
                invitation_lv.dividerHeight = 0
            }

            field = x
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invitations)

        userSuggestionsAdapter = UserProfileListAdapter(this)
        invitedGuestsAdapter = UserProfileListAdapter(this)

        invitation_no_one_text.visibility =
                if(inviteList.isEmpty())
                    View.VISIBLE
                else
                    View.GONE

        invitation_invited_text.visibility =
                if(inviteList.isEmpty())
                    View.GONE
                else
                    View.VISIBLE

        invitation_search_input.apply {
            textChangedListener {
                afterTextChanged {
                    searchMode = !textStr.isBlank()
                    performSearch()
                }
            }
        }

        invitation_search_input.setOnEditorActionListener listener@ { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                if (invitation_search_input.textStr.isNotBlank())
                    performSearch()

            }
            false
        }

        invitation_cancel_button.setOnClickListener {
            finish()
        }

        invitation_lv.setOnItemClickListener { _, view, _, _ ->
            if(searchMode) {
                invitation_search_input.textStr = ""
                searchMode = false

                val user = view.tag as User
                add(user)
            }
        }
    }

    private fun add(user: User) {
        inviteList.add(user)
        invitedGuestsAdapter.add(user)
        invitation_lv.setHeightToWrapContent()
        invitation_no_one_text.visibility =
                if(inviteList.isEmpty())
                    View.VISIBLE
                else
                    View.GONE
        invitation_invited_text.visibility =
                if(inviteList.isEmpty())
                    View.GONE
                else
                    View.VISIBLE
    }

    private fun remove(user: User) {
        inviteList.remove(user)
        invitedGuestsAdapter.remove(user)
        invitation_lv.setHeightToWrapContent()
        invitation_no_one_text.visibility =
                if(inviteList.isEmpty())
                    View.VISIBLE
                else
                    View.GONE
        invitation_invited_text.visibility =
                if(inviteList.isEmpty())
                    View.GONE
                else
                    View.VISIBLE
    }

    /**
     * Primitive search for HARDCODE_MODE purposes
     */
    private fun performSearch() {
        userSuggestionsAdapter.clear()

        HardcodedUsers
                .filter { it.username.startsWith(invitation_search_input.textStr) }
                .filter { it != Auth.user }
                .filter { it !in inviteList }
                .sortedWith(compareBy({it in Auth.user.getFriends()}, { it.username }))
                .forEach { userSuggestionsAdapter.add(it) }

        invitation_lv.apply {
            invalidateViews()
            async(UI) {
                delay(20)
                setHeightToWrapContent()
            }
        }
    }
}