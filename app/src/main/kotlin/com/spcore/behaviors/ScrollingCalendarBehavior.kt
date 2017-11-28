package com.spcore.behaviors

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.MotionEvent

class ScrollingCalendarBehavior(context: Context, attrs: AttributeSet) : AppBarLayout.Behavior(context, attrs) {

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: AppBarLayout, ev: MotionEvent): Boolean {
        return super.onInterceptTouchEvent(parent, child, ev)
    }
}