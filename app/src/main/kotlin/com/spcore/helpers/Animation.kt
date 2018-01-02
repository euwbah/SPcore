package com.spcore.helpers

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.support.design.widget.AppBarLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import com.alamkanak.weekview.WeekView
import com.spcore.listeners.AppBarStateListener

fun onAnimationEnd(foo: (Animator) -> Unit): AnimatorListenerAdapter {
    return object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            foo(animation)
        }
    }
}

/**
 * Makes the calendar dropdown snap and initialises [date_picker_arrow] rotating animation
 * @return the new state of [isAppBarExpanded]
 */
fun initCoolCalendarDropDown(
        date_picker_dropdown_button: RelativeLayout,
        app_bar_layout: AppBarLayout,
        isAppBarExpanded: Boolean,
        date_picker_arrow: ImageView,
        schedule_view: WeekView): Boolean {

    var appBarState: AppBarStateListener.State = AppBarStateListener.State.COLLAPSED
    var isAppBarExpanded = isAppBarExpanded

    date_picker_dropdown_button.setOnClickListener {
        isAppBarExpanded = !isAppBarExpanded
        app_bar_layout.setExpanded(isAppBarExpanded, true)
    }

    app_bar_layout.addOnOffsetChangedListener(
            AppBarStateListener {
                state, prev ->
                appBarState = state
                when(state) {
                    is AppBarStateListener.State.COLLAPSED -> {
                        date_picker_arrow.rotation = -180f
                        isAppBarExpanded = false

                        schedule_view.invalidate()
                    }
                    is AppBarStateListener.State.EXPANDED -> {
                        date_picker_arrow.rotation = 0f
                        isAppBarExpanded = true

                        schedule_view.invalidate()
                    }
                    is AppBarStateListener.State.QUANTUM_FLUX_SUPERPOSITION -> {
                        date_picker_arrow.rotation = (state.expandedness - 1).toFloat() * 180
                    }
                    is AppBarStateListener.State.STUCK_IN_FUTURE_TIMELINE_SUPERPOSITION -> {

                        // It is logical to assusme that if the user wishes to interact
                        // with the AppBarLayout, the user's ultimate intention would be to
                        // toggle the calendar view. As such, the threshold of which a
                        // semi-collapsed/expanded toolbar should constitute as
                        // "to be expanded" or "to be collapsed" should differ
                        // according to the previous stable state of the AppBarLayout

                        val expandingThreshold =
                                if (prev is AppBarStateListener.State.COLLAPSED)
                                    0.2
                                else
                                    0.8

                        val toExpand = state.originalExpandedness >= expandingThreshold
                        isAppBarExpanded = toExpand
                        app_bar_layout.setExpanded(toExpand, true)
                    }
                }
            }
    )
    return isAppBarExpanded
}