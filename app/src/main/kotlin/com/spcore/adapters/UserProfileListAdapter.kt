package com.spcore.adapters

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.mikhaellopez.circularimageview.CircularImageView
import com.spcore.R
import com.spcore.helpers.Auth
import com.spcore.helpers.dpToPx
import com.spcore.helpers.get
import com.spcore.models.User

/**
 * [DeletableUser] is a list item wrapper for the [User] object, which causes
 * the [UserProfileListAdapter] to display a 'X' beside the user profile.
 *
 * When clicked, the user profile will be removed from the adapter and a callback,
 * as defiend by [UserProfileListAdapter.setOnUserDelete], will be invoked with
 * the deleted user as the parameter.
 */
class DeletableUser(user: User) : User(user.adminNo, user.username, user.displayName, user.HARDCODE_MODE_friends)

class UserProfileListAdapter(context: Context, users: MutableList<out Any> = mutableListOf(), val userRoleMapping: Map<User, String> = mapOf()) :
        ArrayAdapter<Any>(context, R.layout.template_user_list_item_layout, users) {

    private var onUserDelete: ((User) -> Unit)? = null

    /**
     * Used to keep track of the most recently added item's position in the list
     * for animation purposes
     */
    private var lastPosition = -1

    /**
     * The way an [ArrayAdapter] works:
     *
     * There are a fixed number of views within a collection (lets call it C),
     * just enough to cover the whole screen plus one more for scrolling's sake.
     * The [convertView] parameter represents a particular `View?` within C,
     * that would be the previous return value from [getView] itself.
     *
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val obj = getItem(position)

        if (obj is User) {
            val user: User = obj
            val isCurrUser = user == Auth.user
            val view = if (convertView == null) {
                val inflater = LayoutInflater.from(context)
                inflater.inflate(R.layout.template_user_list_item_layout, parent, false)
            } else {
                convertView
            }

            view[R.id.profile_pic, CircularImageView::class.java]?.setImageDrawable(user.getProfilePic(context))
            view[R.id.display_name_text, TextView::class.java]?.apply {
                if (isCurrUser) {
                    visibility = View.VISIBLE
                    text = "Me"
                } else {
                    if (user.displayName == null || user.displayName.isBlank()) {
                        visibility = View.GONE
                    } else {
                        visibility = View.VISIBLE
                        text = user.displayName
                    }
                }
            }
            view[R.id.username_text, TextView::class.java]?.apply {
                if (isCurrUser) {
                    visibility = View.GONE
                } else {
                    text = "@${user.username}"
                    visibility = View.VISIBLE

                    if (user.displayName == null || user.displayName.isBlank()) {
                        textSize = 18f
                        setTextColor(0xFF_000000.toInt())
                    } else {
                        textSize = 14f
                        setTextColor(0xAA_000000.toInt())
                    }
                }
            }
            view[R.id.role_text, TextView::class.java]?.apply {
                userRoleMapping[user]?.let {
                    this.visibility = View.VISIBLE
                    this.text = it
                } ?: run {
                    this.visibility = View.GONE
                }
            }
            view[R.id.delete_button, ImageView::class.java]?.apply {
                if (obj is DeletableUser) {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        remove(user)
                        onUserDelete?.invoke(user)
                    }
                } else
                    visibility = View.GONE
            }

            view.tag = user

            return view
        }

        return null
    }

    fun setOnUserDelete(cb: (User) -> Unit) {
        this.onUserDelete = cb
    }
}