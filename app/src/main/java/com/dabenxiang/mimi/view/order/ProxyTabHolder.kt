package com.dabenxiang.mimi.view.order

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import kotlinx.android.synthetic.main.item_tab.view.iv_selected
import kotlinx.android.synthetic.main.item_tab.view.tv_title
import kotlinx.android.synthetic.main.item_tab_favorite_secondary.view.*

class ProxyTabHolder(
    itemView: View,
    listener: IndexViewHolderListener
) : BaseIndexViewHolder<Pair<String, Boolean>>(itemView, listener) {

    private val tvTitle: TextView = itemView.tv_title
    private val vNew: View = itemView.v_new
    private val ivSelected: ImageView = itemView.iv_selected

    init {
        itemView.setOnClickListener { listener.onClickItemIndex(it, index) }
    }

    override fun updated(model: Pair<String, Boolean>?) {
        tvTitle.text = model?.first
        vNew.visibility = if (model?.second == true) View.VISIBLE else View.GONE
    }

    fun setSelected(isSelected: Boolean) {
        ivSelected.visibility =
            when (isSelected) {
                true -> View.VISIBLE
                else -> View.INVISIBLE
            }

        if (isSelected) {
            R.color.color_red_1
        } else {
            R.color.color_tab_unselected_text
        }.also {
            tvTitle.setTextColor(itemView.resources.getColor(it, null))
        }
    }

}