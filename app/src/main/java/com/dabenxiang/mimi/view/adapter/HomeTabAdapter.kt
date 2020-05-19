package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.home.HomeTabHolder

class HomeTabAdapter(private val listener: BaseIndexViewHolder.IndexViewHolderListener, private val isAdult: Boolean) :
    RecyclerView.Adapter<HomeTabHolder>() {

    private var tabList: List<String>? = null
    private var lastSelected = 0

    fun setTabList(src: List<String>, initSelectedIndex: Int) {
        lastSelected = initSelectedIndex
        tabList = src

        notifyDataSetChanged()
    }

    val size = tabList?.size ?: 0

    fun setLastSelectedIndex(index: Int) {
        val oldSelected = lastSelected
        lastSelected = index

        notifyItemChanged(oldSelected)
        notifyItemChanged(lastSelected)
    }

    override fun getItemCount(): Int {
        return tabList?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeTabHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tab, parent, false)
        return HomeTabHolder(view, listener, isAdult)
    }

    override fun onBindViewHolder(holder: HomeTabHolder, position: Int) {
        holder.bind(tabList?.get(position) ?: "", position)
        holder.setSelected(lastSelected == position)
    }
}