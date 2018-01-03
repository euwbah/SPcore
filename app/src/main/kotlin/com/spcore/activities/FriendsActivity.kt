package com.spcore.activities

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import com.spcore.R
import com.spcore.adapters.UserProfileListAdapter
import com.spcore.helpers.Auth
import com.spcore.helpers.HardcodedFriends
import com.spcore.helpers.HardcodedUsers
import com.spcore.helpers.textStr
import com.spcore.models.User
import kotlinx.android.synthetic.main.activity_friends.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.*
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.sdk25.coroutines.onEditorAction

class FriendsActivity : AppCompatActivity() {

    private var addFriendDialog: DialogInterface? = null

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

            val friends = asyncFriends.await().toMutableList()

            friend_list_view.adapter = UserProfileListAdapter(this@FriendsActivity, friends)
        }

        friend_list_view.setOnItemClickListener listener@ { _, view, _, _ ->
            val user = view.tag as? User ?: return@listener

            // Don't let the user click oneself's profile
            if (user != Auth.user)
                startActivity<FriendScheduleActivity>("user" to user)
        }

        friend_list_view.isLongClickable = true
        friend_list_view.setOnItemLongClickListener { _, view, _, _ ->
            val user = view.tag as User
            alert {
                title = "Remove Friend"
                isCancelable = false
                positiveButton("Remove") { removeFriend(user) }
                negativeButton("Cancel") {}
            }.show()
            true
        }

        add_friend_fab.setOnClickListener {
            addFriendDialog = alert {
                title = "Add Friend"
                isCancelable = false
                customView {
                    verticalLayout {
                        val userSearched = editText {
                            hint = "username"
                            imeOptions = EditorInfo.IME_ACTION_DONE
                            setSingleLine()

                            onEditorAction { v, actionId, event ->
                                addFriend(textStr)
                                addFriendDialog?.dismiss()
                            }
                        }.lparams {
                            width = matchParent
                            marginStart = dip(24)
                            marginEnd = dip(24)
                        }


                        positiveButton("Send Request") {
                            addFriend(userSearched.textStr)
                        }
                        negativeButton("Cancel") { /*Leave this empty*/ }
                    }

                }

            }.show()
        }


    }

    private fun addFriend(usrName: String): Int {
        if (usrName.isBlank())
            return -1
        //Check if user exist
        val user = HardcodedUsers.filter {
            it.username == usrName
        }

        if (user.isEmpty()) {
            toast("$usrName not found!")
            return 0
        }
        // check if already friends
        val isAlrFriends = HardcodedFriends.find { it == user[0] }
        if (isAlrFriends != null)
            toast("You are already friends!")
        else {
            HardcodedFriends.add(user[0])
            toast("Friend request sent to ${user[0].username}")
            async(UI) {
                val asyncFriends = bg {
                    Auth.user.getFriends()
                }

                val friends = asyncFriends.await().toMutableList()

                friend_list_view.adapter = UserProfileListAdapter(this@FriendsActivity, friends)
            }
            friend_list_view.invalidate()
        }

        return 1
    }

    private fun removeFriend(user: User) {
        HardcodedFriends.remove(user)
        toast("Successfully removed ${user.username} ☺")
        async(UI) {
            val asyncFriends = bg {
                Auth.user.getFriends()
            }

            val friends = asyncFriends.await().toMutableList()

            friend_list_view.adapter = UserProfileListAdapter(this@FriendsActivity, friends)
        }
        friend_list_view.invalidate()
    }
}
