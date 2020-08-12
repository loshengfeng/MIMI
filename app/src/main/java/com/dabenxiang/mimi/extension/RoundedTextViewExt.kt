package com.dabenxiang.mimi.extension

import com.yulichswift.roundedview.tool.RoundedDrawable
import com.yulichswift.roundedview.widget.RoundedTextView

fun RoundedTextView.setBtnSolidColor(color: Int) {
    val drawable = RoundedDrawable(true)
    drawable.setSolidColorsAndPressedColor(normal = color, pressed = -1, selected = 0, disable = 0)
    background = drawable
}

fun RoundedTextView.setBtnSolidColor(color1: Int, color2: Int, radius: Float) {
    val drawable = RoundedDrawable(false)
    drawable.cornerRadius = radius
    drawable.setSolidColorsAndPressedColor(
        normal = color1,
        pressed = color2,
        selected = 0,
        disable = 0
    )
    background = drawable
}