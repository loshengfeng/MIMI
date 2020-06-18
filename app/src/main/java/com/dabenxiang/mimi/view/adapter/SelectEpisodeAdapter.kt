package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.player.SelectEpisodeHolder

class SelectEpisodeAdapter(private val listener: BaseIndexViewHolder.IndexViewHolderListener, private val isAdult: Boolean) :
    BaseTabAdapter<String, SelectEpisodeHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectEpisodeHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_episode, parent, false)
        return SelectEpisodeHolder(view, listener, isAdult)
    }

    override fun onBindViewHolder(holder: SelectEpisodeHolder, position: Int) {
        holder.bind(tabList?.get(position) ?: "", position)
        holder.setSelected(lastSelected == position)
    }
}