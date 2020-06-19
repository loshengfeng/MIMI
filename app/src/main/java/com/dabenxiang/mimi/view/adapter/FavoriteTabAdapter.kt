package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.favroite.FavoriteTabHolder

class FavoriteTabAdapter(
    private val listener: BaseIndexViewHolder.IndexViewHolderListener,
    private val isPrimary: Boolean
) : BaseTabAdapter<String, FavoriteTabHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteTabHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (isPrimary) {
            true -> FavoriteTabHolder(
                layoutInflater.inflate(
                    R.layout.item_tab_favorite_primary,
                    parent,
                    false
                ), listener, isPrimary
            )
            else -> FavoriteTabHolder(
                layoutInflater.inflate(
                    R.layout.item_tab_favorite_secondary,
                    parent,
                    false
                ), listener, isPrimary
            )
        }
    }

    override fun onBindViewHolder(holder: FavoriteTabHolder, position: Int) {
        holder.bind(tabList?.get(position) ?: "", position)
        holder.setSelected(lastSelected == position)
    }
}