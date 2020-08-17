package com.dabenxiang.mimi.view.player

import android.view.View
import android.widget.TextView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import kotlinx.android.synthetic.main.item_filter.view.*

class SelectEpisodeHolder(itemView: View, listener: IndexViewHolderListener, var isAdult: Boolean) :
    BaseIndexViewHolder<String>(itemView, listener) {

    private val tvTitle: TextView = itemView.tv_title

    init {
        itemView.setOnClickListener {
            listener.onClickItemIndex(it, index)
        }
    }

    override fun updated(model: String?) {
        tvTitle.text = model
    }

    fun setSelected(isSelected: Boolean) {
        when {
            isSelected -> R.color.color_white_1
            isAdult -> R.color.color_white_1_50
            else -> R.color.color_black_1_50
        }.also {
            tvTitle.setTextColor(itemView.resources.getColor(it, null))
        }

        when {
            isSelected -> tvTitle.setBackgroundResource(R.drawable.bg_red_1_radius_6)
            isAdult -> tvTitle.setBackgroundResource(R.drawable.bg_stroke_white_1_50_radius_6)
            else -> tvTitle.setBackgroundResource(R.drawable.bg_stroke_black_1_50_radius_6)
        }
    }
}