package com.dabenxiang.mimi.view.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import com.dabenxiang.mimi.R
import timber.log.Timber

class ActorProfileBehavior: CoordinatorLayout.Behavior<View> {

    constructor()

    constructor(context: Context, attr: AttributeSet) : super(context, attr)

//    var isFirstBorder: Boolean = false

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return true
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if(R.id.actor_profile == child.id){
        when {
            child.translationY > 0 -> {
                child.translationY = 0f
                Timber.d("catkingg 1")
            }

            child.translationY.toInt() < child.marginTop * -1 -> {
                child.translationY = child.marginTop.toFloat() * -1
                Timber.d("catkingg 2")
            }

            child.translationY == child.marginTop.toFloat() * -1 && dy > 0 -> {
                Timber.d("catkingg 3")
            }

            child.translationY == 0f && dy < 0 -> {
                Timber.d("catkingg 4")
            }

            else -> {
                child.translationY = child.translationY + (dy * -1)
                Timber.d("catkingg 5")
            }
        }
        }
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        if (dyConsumed > 0 && dyUnconsumed == 0) {
//            Timber.d("上滑中")
            when (child.id) {
                R.id.actor_profile -> {
                    if(child.visibility == View.VISIBLE){
                        val lp = CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.WRAP_CONTENT, CoordinatorLayout.LayoutParams.WRAP_CONTENT)
                        lp.setMargins(target.marginLeft,target.marginTop - child.height, target.marginRight, target.marginBottom)
                        target.layoutParams = lp
                        child.visibility = View.INVISIBLE
                    }
                }
                R.id.layout_title -> {
                    if(child.visibility == View.VISIBLE){
                        child.visibility = View.INVISIBLE
                    }
                }
                R.id.actor_title -> {
                    if(child.visibility == View.INVISIBLE){
                        child.visibility = View.VISIBLE
                    }
                }
            }
        }
        if (dyConsumed == 0 && dyUnconsumed > 0) {
//            Timber.d("到邊界了還在上滑")
        }
        if (dyConsumed < 0 && dyUnconsumed == 0) {
//            Timber.d("下滑中")
        }
        if (dyConsumed == 0 && dyUnconsumed < 0) {
//            Timber.d("到邊界了還在下滑")

            when (child.id) {
                R.id.actor_profile -> {
                    if(child.visibility == View.INVISIBLE){
                        val lp = CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.WRAP_CONTENT, CoordinatorLayout.LayoutParams.WRAP_CONTENT)
                        lp.setMargins(target.marginLeft,target.marginTop + child.height, target.marginRight, target.marginBottom)
                        target.layoutParams = lp
                        child.visibility = View.VISIBLE
                    }
                }
                R.id.layout_title -> {
                    if(child.visibility == View.INVISIBLE){
                        child.visibility = View.VISIBLE
                    }
                }
                R.id.actor_title -> {
                    if(child.visibility == View.VISIBLE){
                        child.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }
}