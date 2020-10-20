package com.dabenxiang.mimi.view.home.viewholder

import android.view.View
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.vo.BaseVideoItem
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import kotlinx.android.synthetic.main.nested_item_home_statistics.view.*

class VideoViewHolder(itemView: View, onClickListener: IndexViewHolderListener) :
    BaseIndexViewHolder<BaseVideoItem.Video>(itemView, onClickListener) {

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

    override fun updated(model: BaseVideoItem.Video?) {
        if (model == null) {
            tvResolution.visibility = View.INVISIBLE
            tvInfo.visibility = View.INVISIBLE
            tvTitle.visibility = View.INVISIBLE

            Glide.with(itemView.context)
                .load(R.drawable.bg_black_1)
                .into(ivPoster)
        } else {
            tvResolution.visibility = if (model.resolution.isNullOrEmpty()) {
                View.INVISIBLE
            } else {
                tvResolution.text = model.resolution
                View.VISIBLE
            }

            tvInfo.visibility = if (model.info.isNullOrEmpty()) {
                View.INVISIBLE
            } else {
                tvInfo.text = model.info
                View.VISIBLE
            }

            tvTitle.visibility = if (model.title.isNullOrEmpty()) {
                View.INVISIBLE
            } else {
                tvTitle.text = model.title
                View.VISIBLE
            }

                card.setCardBackgroundColor(
                    itemView.resources.getColor(
                        R.color.normal_color_card_background,
                        null
                    )
                )
                tvTitle.setTextColor(itemView.resources.getColor(R.color.normal_color_text, null))

            Glide.with(itemView.context)
                .load(model.imgUrl)
                .into(ivPoster)
        }
    }
}