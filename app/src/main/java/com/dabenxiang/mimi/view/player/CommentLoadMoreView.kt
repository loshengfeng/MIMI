package com.dabenxiang.mimi.view.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.chad.library.adapter.base.loadmore.BaseLoadMoreView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.enums.CommentViewType

class CommentLoadMoreView(private val type: CommentViewType) :
    BaseLoadMoreView() {

    override fun getRootView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_comment_load_more, parent, false)
    }

    override fun getLoadingView(holder: BaseViewHolder): View {
        holder.setBackgroundResource(R.id.view_load_more_loading, getBackground())
        return holder.getView(R.id.view_load_more_loading)
    }

    override fun getLoadComplete(holder: BaseViewHolder): View {
        holder.setTextColorRes(R.id.tv_load_more_complete, getTextColorRes())

        val textView = holder.getView<TextView>(R.id.tv_load_more_complete)
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
            0,
            0,
            R.drawable.btn_arrowdown_gray_n,
            0
        )

        holder.setBackgroundResource(R.id.view_load_more_complete, getBackground())

        return holder.getView(R.id.view_load_more_complete)
    }

    override fun getLoadEndView(holder: BaseViewHolder): View {
        holder.setTextColorRes(R.id.tv_load_more_end, getTextColorRes())
        holder.setBackgroundResource(R.id.bg_load_more_end, getBackground())
        return holder.getView(R.id.view_load_more_end)
    }

    override fun getLoadFailView(holder: BaseViewHolder): View {
        holder.setTextColorRes(R.id.tv_load_more_fail, getTextColorRes())
        holder.setBackgroundResource(R.id.bg_load_more_fail, getBackground())
        return holder.getView(R.id.view_load_more_fail)
    }


    private fun getTextColorRes() = R.color.color_black_1_50

    private fun getBackground(): Int {
        return when (type) {
            CommentViewType.CLIP -> R.color.transparent
            CommentViewType.VIDEO -> R.drawable.bg_comment_bottom_radius_10
            else -> R.drawable.bg_adult_comment_bottom_radius_10
        }
    }

}