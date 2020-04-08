package com.dabenxiang.mimi.view.favorite

import android.view.View
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_favorite.view.*


class FavoriteViewHolder(view: View) : BaseViewHolder(view) {
    val tvNo = itemView.tv_no!!
    val ivPhoto = itemView.iv_photo!!
    val tvInfo = itemView.tv_info!!
    val tvTitle = itemView.tv_title!!
    val tvContent = itemView.tv_content!!
    val ivFavorite = itemView.iv_favorite!!
}