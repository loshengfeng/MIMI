package com.dabenxiang.mimi.widget.view

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import timber.log.Timber

class ActorsGridSpaceItemDecoration(
    val context: Context
) : RecyclerView.ItemDecoration() {

    var minCategoryPos: Int = 0

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildLayoutPosition(view)
        when(view.id){
            R.id.cl_actor_videos -> {
                outRect.left = GeneralUtils.dpToPx(context, 0)
            }
            R.id.cl_category -> {
                if(minCategoryPos == 0) minCategoryPos = position

                outRect.top = if((position - minCategoryPos)/4 == 0) GeneralUtils.dpToPx(context, 15)
                else GeneralUtils.dpToPx(context, 20)

                when((position - minCategoryPos)%4){
                    0 -> {
                        outRect.left = GeneralUtils.dpToPx(context, 8)
                        outRect.right = GeneralUtils.dpToPx(context, 8)
                    }
                    3 -> {
                        outRect.left = GeneralUtils.dpToPx(context, 8)
                        outRect.right = GeneralUtils.dpToPx(context, 8)
                    }
                    else -> {
                        outRect.left = GeneralUtils.dpToPx(context, 8)
                        outRect.right = GeneralUtils.dpToPx(context, 8)
                    }
                }

//                Timber.d("position $position with ( ${outRect.top}, ${outRect.left}, ${outRect.right})")
            }
            else -> {
                outRect.top = GeneralUtils.dpToPx(context, 10)
                outRect.left = GeneralUtils.dpToPx(context, 20)
            }
        }
    }
}