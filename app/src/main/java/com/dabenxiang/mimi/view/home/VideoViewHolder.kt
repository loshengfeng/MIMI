package com.dabenxiang.mimi.view.home

import android.view.View
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.nested_item_home_categories.view.*

class VideoViewHolder(itemView: View) : BaseViewHolder(itemView) {
    val tvResolution = itemView.tv_resolution!!
    val tvInfo = itemView.tv_info!!
    val tvTitle = itemView.tv_title!!
    val ivPoster = itemView.iv_poster!!
}