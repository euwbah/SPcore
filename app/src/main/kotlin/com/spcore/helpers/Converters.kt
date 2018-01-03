package com.spcore.helpers

import android.content.res.Resources
import android.graphics.Color
import android.util.DisplayMetrics


fun Double.dpToPx(): Double {
    val metrics = Resources.getSystem().displayMetrics
    return this * metrics.densityDpi.toDouble() / DisplayMetrics.DENSITY_DEFAULT
}

fun Double.pxToDp(): Double {
    val metrics = Resources.getSystem().displayMetrics
    return this / (metrics.densityDpi.toDouble() / DisplayMetrics.DENSITY_DEFAULT)
}


fun Float.dpToPx(): Float {
    val metrics = Resources.getSystem().displayMetrics
    return this * metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT
}

fun Float.pxToDp(): Float {
    val metrics = Resources.getSystem().displayMetrics
    return this / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}


fun Int.dpToPx(): Int {
    val metrics = Resources.getSystem().displayMetrics
    return (this * metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT).toInt()
}

fun Int.pxToDp(): Int {
    val metrics = Resources.getSystem().displayMetrics
    return (this / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
}