package com.dabenxiang.mimi.view.favorite

import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.holder.FavoriteItem
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import com.dabenxiang.mimi.view.listener.AdapterEventListener
import kotlinx.android.synthetic.main.item_favorite.view.*
import timber.log.Timber

class FavoriteViewHolder(
    view: View,
    listener: AdapterEventListener<FavoriteItem>
) : BaseAnyViewHolder<FavoriteItem>(view) {
    private val tvNo = itemView.tv_no!!
    private val ivPhoto = itemView.iv_photo!!
    private val tvInfo = itemView.tv_info!!
    private val tvTitle = itemView.tv_title!!
    private val tvSubtitle = itemView.tv_subtitle!!
    private val ivFavorite = itemView.iv_favorite!!

    init {
        view.setOnClickListener { data?.let { it -> listener.onItemClick(view, it) } }
    }

    override fun updated() {
        Timber.d("${FavoriteViewHolder::class.java.simpleName}_updated")
        tvNo.text = data?.no
        ivPhoto.setImageResource(R.color.color_red_1)
        tvInfo.text = data?.info
        tvTitle.text = data?.title
        tvSubtitle.text = data?.subtitle
        ivFavorite.setImageResource(R.drawable.ic_btn_favorite_white_s)
    }
}