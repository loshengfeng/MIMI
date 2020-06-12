package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.home.SelectStreamHolder

class SelectStreamAdapter(private val listener: BaseIndexViewHolder.IndexViewHolderListener, private val isAdult: Boolean) :
    BaseTabAdapter<SelectStreamHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectStreamHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stream, parent, false)
        return SelectStreamHolder(view, listener, isAdult)
    }

    override fun onBindViewHolder(holder: SelectStreamHolder, position: Int) {
        holder.bind(tabList?.get(position) ?: "", position)
        holder.setSelected(lastSelected == position)
    }
}