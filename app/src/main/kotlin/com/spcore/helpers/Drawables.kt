package com.spcore.helpers

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable


/**
 * Resizes a drawable and converts
 *
 * @param newWidth The new width of the drawable in **px**
 * @param newHeight The new height of the drawable in **px**
 * @param resources The resource set which provides device-specific dimension units rather than
 *                  default ones.
 *                  If not provided, it will use the default system resources from `Resources.getSystem()`, but
 *                  this is akin to deprecated behaviour, and will not work properly if the phone
 *                  orientation is changed.
 */
fun Drawable.resizeImage(newWidth: Float, newHeight: Float, resources: Resources = Resources.getSystem()) : Drawable {
    val bmp = this.toBitmap()

    val scaleWidth = newWidth / bmp.width
    val scaleHeight = newHeight / bmp.height

    val matrix = Matrix()
    matrix.postScale(scaleWidth, scaleHeight)

    val resizedBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
    return BitmapDrawable(resources, resizedBmp)
}


fun Drawable.toBitmap(): Bitmap {

    if (this is BitmapDrawable) {
        val bitmapDrawable = this
        if (bitmapDrawable.getBitmap() != null) {
            return bitmapDrawable.getBitmap()
        }
    }

    val bitmap: Bitmap = if (this.intrinsicWidth <= 0 || this.intrinsicHeight <= 0) {
        Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
    } else {
        Bitmap.createBitmap(this.intrinsicWidth, this.intrinsicHeight, Bitmap.Config.ARGB_8888)
    }

    val canvas = Canvas(bitmap)
    this.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
    this.draw(canvas)
    return bitmap
}

