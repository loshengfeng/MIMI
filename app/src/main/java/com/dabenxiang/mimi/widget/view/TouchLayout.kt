package com.dabenxiang.mimi.widget.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout


class TouchLayout(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    override fun addView(child: View?, params: ViewGroup.LayoutParams?) {
        super.addView(child, params)
        setTouchDelegate(child)
    }

    private fun setTouchDelegate(child: View?) {
        if (touchDelegate == null) {
            child?.let {
                if (it.isClickable) {
                    val size = convertDpToPixel(40f, context)
                    this.touchDelegate = TouchDelegate(Rect(0, 0, size.toInt(), size.toInt()), it)
                }
            }
        }
    }

    private fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * getDensity(context)
    }

    private fun getDensity(context: Context): Float {
        val metrics = context.resources.displayMetrics
        return metrics.density
    }
}