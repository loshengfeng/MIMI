package com.dabenxiang.mimi.view.home

import android.view.View
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.holder.VideoHolderItem
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import kotlinx.android.synthetic.main.nested_item_home_categories.view.*

class VideoViewHolder(itemView: View, onClickListener: IndexViewHolderListener) : BaseIndexViewHolder<VideoHolderItem>(itemView, onClickListener) {

    private val tvResolution = itemView.tv_resolution!!
    private val tvInfo = itemView.tv_info!!
    private val tvTitle = itemView.tv_title!!
    private val ivPoster = itemView.iv_poster!!

    init {
        ivPoster.setOnClickListener {
            listener.onClickItemIndex(it, index)
        }
    }

    override fun updated(model: VideoHolderItem?) {
        if (model == null) {
            tvResolution.text = ""
            tvInfo.text = ""
            tvTitle.text = ""

            Glide.with(itemView.context)
                .load(R.drawable.bg_black_1)
                .into(ivPoster)
        } else {
            tvResolution.text = model.resolution
            tvInfo.text = model.info
            tvTitle.text = model.title

            Glide.with(itemView.context)
                .load(model.imgUrl)
                .into(ivPoster)
        }
    }
}