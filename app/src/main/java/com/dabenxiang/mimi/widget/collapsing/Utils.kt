package com.dabenxiang.mimi.widget.collapsing

import android.content.Context
import android.view.View
import androidx.annotation.DimenRes

fun normalize(
        oldValue: Float,
        newMin: Float, newMax: Float,
        oldMin: Float = 0f, oldMax: Float = 1f
): Float = newMin + ((oldValue - oldMin) * (newMax - newMin)) / (oldMax - oldMin)

fun View.pixels(@DimenRes dimen: Int): Float = resources.getDimensionPixelOffset(dimen).toFloat()

fun Context.pixels(@DimenRes dimen: Int): Float = resources.getDimensionPixelOffset(dimen).toFloat()

fun View.setHeight(height: Int) {
    val lp = layoutParams
    lp.height = height
    layoutParams = lp
}
