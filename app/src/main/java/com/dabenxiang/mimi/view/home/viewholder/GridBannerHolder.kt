package com.dabenxiang.mimi.view.home.viewholder

import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.model.holder.BaseVideoItem
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import kotlinx.android.synthetic.main.item_banner.view.*

class GridBannerHolder(itemView: View, onClickListener: IndexViewHolderListener) :
    BaseIndexViewHolder<BaseVideoItem.Banner>(itemView, onClickListener) {
    private val ivPoster: ImageView = itemView.iv_poster

    override fun updated(model: BaseVideoItem.Banner?) {
        Glide.with(itemView.context)
            .load(model?.imgUrl)
            .into(ivPoster)
    }
}