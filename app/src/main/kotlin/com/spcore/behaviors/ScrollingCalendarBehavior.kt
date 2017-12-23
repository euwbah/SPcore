package com.spcore.behaviors

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.alamkanak.weekview.WeekView

class ScrollingCalendarBehavior(context: Context, attrs: AttributeSet) : AppBarLayout.Behavior(context, attrs) {

    lateinit private var scheduleView: WeekView

    private val gestureDetector = GestureDetector(context, GestDetector())

    override fun layoutDependsOn(parent: CoordinatorLayout?, child: AppBarLayout?, dependency: View?): Boolean {

        if (dependency is WeekView) {
            scheduleView = dependency
//            Log.d("WEEK VIEW FOUND", "JA JAJAJAJAJ")
        }

        return super.layoutDependsOn(parent, child, dependency)
    }

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: AppBarLayout, ev: MotionEvent): Boolean {

        // update gesture detector
        gestureDetector.onTouchEvent(ev)

//        Log.d("FIRST VISIBLE HOUR", scheduleView.firstVisibleHour.toString())

        return super.onInterceptTouchEvent(parent, child, ev)
    }

    inner class GestDetector : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(ev: MotionEvent): Boolean {
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            return true
        }
    }
}