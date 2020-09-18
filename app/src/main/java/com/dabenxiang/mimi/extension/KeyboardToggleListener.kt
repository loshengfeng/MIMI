package com.dabenxiang.mimi.extension

import android.app.Activity
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import kotlin.math.roundToInt

infix fun Activity.addKeyboardToggleListener(onKeyboardToggleAction: (shown: Boolean) -> Unit): KeyboardToggleListener? {
    val root = findViewById<View>(android.R.id.content)
    val listener = KeyboardToggleListener(root, onKeyboardToggleAction)
    return root?.viewTreeObserver?.run {
        addOnGlobalLayoutListener(listener)
        listener
    }
}

open class KeyboardToggleListener(
    private val root: View?,
    private val onKeyboardToggleAction: (shown: Boolean) -> Unit
) : ViewTreeObserver.OnGlobalLayoutListener {
    private var shown = false
    override fun onGlobalLayout() {
        root?.run {
            val heightDiff = rootView.height - height
            val keyboardShown = heightDiff > this dpToPx 200f
            if (shown != keyboardShown) {
                onKeyboardToggleAction.invoke(keyboardShown)
                shown = keyboardShown
            }
        }
    }
}

infix fun View.dpToPx(dp: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).roundToInt()