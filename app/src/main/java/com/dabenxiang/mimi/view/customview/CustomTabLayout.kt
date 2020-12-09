package com.dabenxiang.mimi.view.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.dabenxiang.mimi.R
import com.google.android.material.tabs.TabLayout

/**
 * TODO: document your custom view class.
 */
class CustomTabLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TabLayout(context, attrs, defStyleAttr) {

    init {
        this.getTabAt(0)?.customView?.findViewById<TextView>(R.id.tv_title)?.run {
            setupTextViewSelected(true, this)
        }

        addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: Tab?) {
                tab?.customView?.findViewById<TextView>(R.id.tv_title)?.run {
                    setupTextViewSelected(true, this)
                }
            }

            override fun onTabUnselected(tab: Tab?) {
                tab?.customView?.findViewById<TextView>(R.id.tv_title)?.run {
                    setupTextViewSelected(false, this)
                }
            }

            override fun onTabReselected(tab: Tab?) {
            }
        })
    }

    private fun setupTextViewSelected(isSelected: Boolean, textView: TextView) {
        if (isSelected) {
            textView.setTypeface(null, Typeface.BOLD)
            textView.setTextColor(context.getColor(R.color.color_black_1))
        } else {
            textView.setTypeface(null, Typeface.NORMAL)
            textView.setTextColor(context.getColor(R.color.color_black_1_50))
        }
    }

    fun setTitle(title: String, position: Int) {
        this.getTabAt(position)?.customView?.findViewById<TextView>(R.id.tv_title)?.let {
            it.text = title
        }
    }
}