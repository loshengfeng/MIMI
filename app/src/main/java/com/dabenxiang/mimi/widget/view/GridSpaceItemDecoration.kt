package com.dabenxiang.mimi.widget.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpaceItemDecoration(
    private val span: Int,
    private val spacing: Int,
    private val edgePadding: Int,
    private val adInterval: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildLayoutPosition(view)
        val lEdgeRule = if ((position / (adInterval + 1)) % 2 == 0) 1 else 0
        val rEdgeRule = if ((position / (adInterval + 1)) % 2 == 0) 0 else 1

        when {
            position % (adInterval + 1) == 0 -> {
            }
            position % span == lEdgeRule -> {
                outRect.top = edgePadding
                outRect.left = edgePadding
                outRect.right = spacing / 2
            }
            position % span == rEdgeRule -> {
                outRect.top = edgePadding
                outRect.left = spacing / 2
                outRect.right = edgePadding
            }
            else -> {
                outRect.top = edgePadding
                outRect.left = spacing / 2
                outRect.right = spacing / 2
            }
        }
    }
}