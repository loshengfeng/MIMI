package com.dabenxiang.mimi.widget.collapsing

import androidx.annotation.FloatRange
import android.view.animation.Interpolator

class ReverseInterpolator(private val interpolator: Interpolator) : Interpolator {
    override fun getInterpolation(input: Float): Float = interpolator.getInterpolation(1 - input)
}

class ThresholdInterpolator(
    @FloatRange(from = 0.0, to = 1.0) private val min: Float,
    @FloatRange(from = 0.0, to = 1.0) private val max: Float,
    private val interpolator: Interpolator? = null
) : Interpolator {
    override fun getInterpolation(input: Float): Float = when {
        input <= min -> min
        input >= max -> max
        else -> interpolator?.getInterpolation(input) ?: input
    }
}

class TurnBackInterpolator(
        private val interpolator: Interpolator,
        @FloatRange(from = 0.0, to = 1.0) private val turnRation: Float = 0f
) : Interpolator {
    override fun getInterpolation(input: Float): Float = when {
        input <= turnRation -> interpolator.getInterpolation(input)
        else -> interpolator.getInterpolation(1f - input)
    }
}
