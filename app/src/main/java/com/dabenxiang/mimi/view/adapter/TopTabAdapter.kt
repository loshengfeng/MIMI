package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.home.HomeTabHolder

class TopTabAdapter(
    private val listener: BaseIndexViewHolder.IndexViewHolderListener,
    private val isAdult: Boolean,
    private val isFavorite: Boolean = false
) :
    BaseTabAdapter<String, HomeTabHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeTabHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = when(isFavorite) {
            true -> layoutInflater.inflate(R.layout.item_tab_favorite, parent, false)
            else -> layoutInflater.inflate(R.layout.item_tab, parent, false)
        }
        return HomeTabHolder(view, listener, isAdult)
    }

    override fun onBindViewHolder(holder: HomeTabHolder, position: Int) {
        holder.bind(tabList?.get(position) ?: "", position)
        holder.setSelected(lastSelected == position)
    }
}