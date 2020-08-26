package com.dabenxiang.mimi.view.home.viewholder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.home.category.CategoriesFragment
import kotlinx.android.synthetic.main.item_filter.view.*
import timber.log.Timber
import tw.gov.president.utils.general.app.GeneralUtils
import kotlin.math.roundToInt

class FilterTabHolder(itemView: View, listener: FilterTabHolderListener, var isAdult: Boolean) :
    BaseIndexViewHolder<String>(itemView, object : IndexViewHolderListener {
        override fun onClickItemIndex(view: View, index: Int) {}
    }) {

    private val tvTitle: TextView = itemView.tv_title
    private var isDisable = false

    interface FilterTabHolderListener {
        fun onClickItemIndex(view: View, index: Int, isDisable: Boolean = false)
    }

    init {
        itemView.setOnClickListener {
            listener.onClickItemIndex(it, index, isDisable)
        }
    }

    override fun updated(model: String?) {
        if (model == CategoriesFragment.TEXT_ALL && index == 0) {
            tvTitle.visibility = View.GONE
        } else {
            tvTitle.visibility = View.VISIBLE
        }
        tvTitle.text = model
    }

    fun setSelected(isSelected: Boolean) {
        when {
            isSelected -> R.color.color_white_1
            isAdult -> R.color.adult_color_text
            else -> R.color.normal_color_text
        }.also {
            tvTitle.setTextColor(itemView.resources.getColor(it, null))
        }
    }

    fun setTitleStyle(isSelected: Boolean, isDisable: Boolean) {
        this.isDisable = isDisable
        when {
            isDisable -> R.color.color_gray_11
            isSelected -> R.color.color_white_1
            isAdult -> R.color.adult_color_text
            else -> R.color.normal_color_text
        }.also {
            tvTitle.setTextColor(itemView.resources.getColor(it, null))
        }

        when {
            isSelected -> tvTitle.setBackgroundResource(R.drawable.bg_red_1_radius_6)
            else -> tvTitle.background = null
        }
    }
}