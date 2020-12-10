package com.dabenxiang.mimi.view.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.dabenxiang.mimi.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_mimi_home.*

/**
 * Custom TabLayout with bold text style
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
                tab?.customView?.findViewById<TextView>(R.id.tv_title)?.run {
                    setupTextViewSelected(true, this)
                }
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
}