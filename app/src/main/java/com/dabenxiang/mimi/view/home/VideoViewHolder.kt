package com.dabenxiang.mimi.view.home

import android.view.View
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.model.holder.VideoHolderItem
import com.dabenxiang.mimi.view.adapter.HomeAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import kotlinx.android.synthetic.main.nested_item_home_categories.view.*

class VideoViewHolder(itemView: View, listener: HomeAdapter.EventListener) : BaseAnyViewHolder<VideoHolderItem>(itemView) {
    private val tvResolution = itemView.tv_resolution!!
    private val tvInfo = itemView.tv_info!!
    private val tvTitle = itemView.tv_title!!
    private val ivPoster = itemView.iv_poster!!

    init {
        ivPoster.setOnClickListener {
            data?.also { data ->
                listener.onVideoClick(it, data)
            }
        }
    }

    override fun updated() {
        tvResolution.text = data?.resolution
        tvInfo.text = data?.info
        tvTitle.text = data?.title

        Glide.with(itemView.context)
            .load(data?.imgUrl)
            .into(ivPoster)
    }
}