package com.dabenxiang.mimi.view.order

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.adapter.BaseTabAdapter
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder

class ProxyTabAdapter(
    private val listener: BaseIndexViewHolder.IndexViewHolderListener
) : BaseTabAdapter<Pair<String, Boolean>, ProxyTabHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProxyTabHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ProxyTabHolder(
                layoutInflater.inflate(
                    R.layout.item_tab_favorite_secondary,
                    parent,
                    false
                ), listener
            )
    }

    override fun onBindViewHolder(holder: ProxyTabHolder, position: Int) {
        holder.bind(tabList?.get(position), position)
        holder.setSelected(lastSelected == position)
    }
}