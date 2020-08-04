package com.dabenxiang.mimi.view.home.viewholder

import android.view.View
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.model.holder.CarouselHolderItem
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.adapter.HomeAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import kotlinx.android.synthetic.main.nested_item_carousel.view.*

class CarouselViewHolder(itemView: View, listener: HomeAdapter.EventListener, isAdult: Boolean) :
    BaseAnyViewHolder<CarouselHolderItem>(itemView) {
    private val ivPoster = itemView.iv_poster

    init {
        ivPoster.setOnClickListener {
            // TODO: 設定影片來源
            listener.onVideoClick(it, PlayerItem.parser(data!!, isAdult))
        }
    }

    override fun updated() {
        Glide.with(itemView.context)
            .load(data?.cover)
            .into(ivPoster)
    }
}