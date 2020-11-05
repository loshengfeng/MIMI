package com.dabenxiang.mimi.view.home.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import kotlinx.android.synthetic.main.item_tab.view.*

class HomeTabHolder(itemView: View, listener: IndexViewHolderListener) :
    BaseIndexViewHolder<String>(itemView, listener) {

    private val tvTitle: TextView = itemView.tv_title
    private val ivSelected: ImageView = itemView.iv_selected

    init {
        itemView.setOnClickListener {
            listener.onClickItemIndex(it, index)
        }
    }

    override fun updated(model: String?) {
        tvTitle.text = model
    }

    fun setSelected(isSelected: Boolean) {
        ivSelected.visibility =
            when (isSelected) {
                true -> View.VISIBLE
                else -> View.INVISIBLE
            }

        if (isSelected) {
            R.color.normal_color_text
        } else {
            R.color.color_tab_unselected_text
        }.let {
            tvTitle.setTextColor(itemView.resources.getColor(it, null))
        }
    }
}