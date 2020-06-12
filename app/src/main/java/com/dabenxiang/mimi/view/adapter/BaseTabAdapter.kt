package com.dabenxiang.mimi.view.adapter

import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder

abstract class BaseTabAdapter<VH : BaseIndexViewHolder<String>> : RecyclerView.Adapter<VH>() {

    protected var tabList: List<String>? = null
    protected var lastSelected = 0

    fun submitList(src: List<String>, initSelectedIndex: Int) {
        lastSelected = initSelectedIndex
        tabList = src

        notifyDataSetChanged()
    }

    fun setLastSelectedIndex(index: Int) {
        val oldSelected = lastSelected
        lastSelected = index

        if (oldSelected >= 0)
            notifyItemChanged(oldSelected)

        if (lastSelected >= 0)
            notifyItemChanged(lastSelected)
    }

    override fun getItemCount(): Int {
        return tabList?.size ?: 0
    }
}