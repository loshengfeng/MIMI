package com.dabenxiang.mimi.widget.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.view.generalvideo.GeneralVideoAdapter
import com.dabenxiang.mimi.view.generalvideo.GeneralVideoAdapter.Companion.VIEW_TYPE_AD

class GridSpaceItemDecoration(
    private val spacing: Int,
    private val edgePadding: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val span = 2

        val position = parent.getChildLayoutPosition(view)
        val lEdgeRule = 0
        val rEdgeRule = 1

        when {
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