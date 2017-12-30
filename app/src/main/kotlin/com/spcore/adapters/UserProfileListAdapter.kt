package com.spcore.adapters

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mikhaellopez.circularimageview.CircularImageView
import com.spcore.R
import com.spcore.helpers.dpToPx
import com.spcore.helpers.get
import com.spcore.models.User

class UserProfileListAdapter(context: Context, val users: List<User>) :
        ArrayAdapter<User>(context, R.layout.template_user_list_item_layout, users) {

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
        val user: User = getItem(position) ?: return null

        val view = if(convertView == null) {
            val inflater = LayoutInflater.from(context)
            inflater.inflate(R.layout.template_user_list_item_layout, parent, false)
        } else {
            convertView
        }

        view[R.id.profile_pic, CircularImageView::class.java]?.setImageDrawable(user.getProfilePic(context))
        view[R.id.display_name_text, TextView::class.java]?.apply {
            if(user.displayName == null)
                visibility = View.GONE
            else {
                visibility = View.VISIBLE
                text = user.displayName
            }
        }
        view[R.id.username_text, TextView::class.java]?.apply {
            text = user.username
            textSize =
                    if(user.displayName == null)
                        18f
                    else
                        14f
        }

        view.startAnimation(AnimationUtils.loadAnimation(context,
                if(position > lastPosition) R.anim.slide_up_in else R.anim.slide_down_in))

        return view
    }
}