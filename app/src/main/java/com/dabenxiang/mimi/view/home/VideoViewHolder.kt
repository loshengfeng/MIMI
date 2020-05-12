package com.dabenxiang.mimi.view.home

import android.view.View
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.holder.VideoHolderItem
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import kotlinx.android.synthetic.main.nested_item_home_categories.view.*

class VideoViewHolder(itemView: View, onClickListener: IndexViewHolderListener) : BaseIndexViewHolder<VideoHolderItem>(itemView, onClickListener) {

    private val card = itemView.layout_card!!
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

            if (model.isAdult) {
                card.setCardBackgroundColor(itemView.resources.getColor(R.color.adult_color_card_background, null))
                tvTitle.setTextColor(itemView.resources.getColor(R.color.adult_color_text, null))
            } else {
                card.setCardBackgroundColor(itemView.resources.getColor(R.color.normal_color_card_background, null))
                tvTitle.setTextColor(itemView.resources.getColor(R.color.normal_color_text, null))
            }

            Glide.with(itemView.context)
                .load(model.imgUrl)
                .into(ivPoster)
        }
    }
}