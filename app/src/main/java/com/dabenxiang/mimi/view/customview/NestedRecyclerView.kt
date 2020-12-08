package com.dabenxiang.mimi.view.customview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import kotlin.math.abs

/**
 * To overcome same direct recycler view and parent
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
    var allowedParentIntercept = false

    init {
        attrs?.run {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.NestedRecyclerView, 0, 0)
            allowedParentIntercept = typedArray.getBoolean(R.styleable.DateEditText_autoCorrect, false)
            typedArray.recycle()
        }
    }

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        val x = e?.x?.toInt() ?: 0
        val y = e?.y?.toInt() ?: 0
        val itemCount = adapter?.itemCount ?: 0
        val position = (layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        when(e?.action) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = abs(x - lastX)
                if (deltaX > MIN_DISTANCE && itemCount > 1) {
                    when(allowedParentIntercept) {
                        true -> {
                            when (position) {
                                0 -> {
                                    //第一個item: 右滑 parent攔截事件
                                    parent.requestDisallowInterceptTouchEvent(x < lastX)
                                }
                                (itemCount - 1) -> {
                                    //最後一個item: 左滑 parent攔截事件
                                    parent.requestDisallowInterceptTouchEvent(x > lastX)
                                }
                                else -> {
                                    //其餘item: 不允許parent攔截事件
                                    parent.requestDisallowInterceptTouchEvent(true)
                                }
                            }
                        }
                        else -> {
                            //default不處理位置問題
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