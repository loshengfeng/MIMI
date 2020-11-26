package com.dabenxiang.mimi.view.home.viewholder

import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.model.vo.BaseVideoItem
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.item_ad.view.*

class GridAdHolder(itemView: View, onClickListener: IndexViewHolderListener) :
    BaseIndexViewHolder<BaseVideoItem.Banner>(itemView, onClickListener) {

    private val ivPoster: ImageView = itemView.iv_ad

    override fun updated(model: BaseVideoItem.Banner?) {
        Glide.with(itemView.context)
            .load(model?.adItem?.href)
            .into(ivPoster)

        ivPoster.setOnClickListener { view ->
            GeneralUtils.openWebView(view.context, model?.adItem?.target ?: "")
        }
    }
}