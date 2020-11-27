package com.dabenxiang.mimi.widget.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpaceItemDecoration(
    private val span: Int,
    private val spacing: Int,
    private val edgePadding: Int,
    private val ignoreFirst: Boolean = false
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildLayoutPosition(view)
        val lEdgeRule = if (ignoreFirst) 1 else 0
        val rEdgeRule = if (ignoreFirst) 0 else span - 1

        when {
            ignoreFirst && position == 0 -> {
            }
            position % span == lEdgeRule -> {
                outRect.left = edgePadding
                outRect.right = spacing / 2
            }
            position % span == rEdgeRule -> {
                outRect.left = spacing / 2
                outRect.right = edgePadding
            }
            else -> {
                outRect.left = spacing / 2
                outRect.right = spacing / 2
            }
        }
    }
}