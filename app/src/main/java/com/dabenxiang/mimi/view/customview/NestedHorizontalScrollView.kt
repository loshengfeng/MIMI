package com.dabenxiang.mimi.view.customview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.HorizontalScrollView

class NestedHorizontalScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
//        when(e?.action) {
//            MotionEvent.ACTION_DOWN -> {
//                parent.requestDisallowInterceptTouchEvent(false)
//            }
//            MotionEvent.ACTION_MOVE -> {
//                parent.requestDisallowInterceptTouchEvent(canScroll())
//            }
//        }
        parent.requestDisallowInterceptTouchEvent(false)
        return super.onInterceptTouchEvent(e)
    }

    private fun canScroll(): Boolean {
        val child: View = this.getChildAt(0) as View
        val childWidth: Int = child.width
        return this.width < childWidth + this.paddingLeft + this.paddingRight
    }
}