package com.dabenxiang.mimi.widget.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class VideosSpaceItemDecoration(
    private val spacing: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.right = spacing / 2
        if (parent.getChildAdapterPosition(view) != 0)
            outRect.left = spacing / 2
    }
}