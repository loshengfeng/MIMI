package com.dabenxiang.mimi.widget.view

import com.yulichswift.roundedview.tool.RoundedDrawable
import com.yulichswift.roundedview.widget.RoundedTextView

fun RoundedTextView.setBtnSolidDolor(color: Int) {
    val drawable = RoundedDrawable(true)
    drawable.setSolidColorsAndPressedGrayer(color)
    background = drawable
}

fun RoundedTextView.setBtnSolidDolor(color1: Int, color2: Int, radius: Float) {
    val drawable = RoundedDrawable(false)
    drawable.cornerRadius = radius
    drawable.setSolidColorsAndPressedColor(color1, color2)
    background = drawable
}