package com.dabenxiang.mimi.view.customview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * TODO: document your custom view class.
 */
class NestedRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
    companion object {
        private const val MIN_DISTANCE = 5
    }
    var lastX = 0
    var lastY = 0

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        val x = e?.x?.toInt() ?: 0
        val y = e?.y?.toInt() ?: 0
        val itemCount = adapter?.itemCount ?: 0
        val position = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        when(e?.action) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = abs(x - lastX)
                if (deltaX > MIN_DISTANCE && itemCount > 1) {
                    when (position) {
                        0 -> {
                            parent.requestDisallowInterceptTouchEvent(x < lastX)
                        }
                        (itemCount - 1) -> {
                            parent.requestDisallowInterceptTouchEvent(x > lastX)
                        }
                        else -> {
                            parent.requestDisallowInterceptTouchEvent(true)
                        }
                    }
                } else {
                    parent.requestDisallowInterceptTouchEvent(false)
                }

            }
        }

        lastX = x
        lastY = y
        return super.onInterceptTouchEvent(e)

    }
}