package com.dabenxiang.mimi.view.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.marginTop

class FilterBehavior : CoordinatorLayout.Behavior<View> {

    constructor()

    constructor(context: Context, attr: AttributeSet) : super(context, attr)

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return true
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        when {
            child.translationY > 0 -> {
                child.translationY = 0f
            }

            child.translationY.toInt() < child.marginTop * -1 -> {
                child.translationY = child.marginTop.toFloat() * -1
//                child.setBackgroundResource(R.drawable.bg_white_1_bottom_radius_20)
            }

            child.translationY == child.marginTop.toFloat() * -1 && dy > 0 -> {
//                child.setBackgroundResource(R.drawable.bg_white_1_bottom_radius_20)
            }

            child.translationY == 0f && dy < 0 -> {
//                child.setBackgroundColor(child.context.getColor(R.color.color_gray_5))
            }

            else -> {
                child.translationY = child.translationY + (dy * -1)
//                child.setBackgroundColor(child.context.getColor(R.color.color_gray_5))
            }
        }
    }
}