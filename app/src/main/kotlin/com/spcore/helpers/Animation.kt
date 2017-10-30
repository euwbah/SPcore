package com.spcore.helpers

import android.animation.Animator
import android.animation.AnimatorListenerAdapter

fun onAnimationEnd(foo: (Animator) -> Unit): AnimatorListenerAdapter {
    return object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            foo(animation)
        }
    }
}