package com.dabenxiang.mimi.view.adapter

import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder

abstract class BaseTabAdapter<M: Any, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    protected var tabList: List<M>? = null
    protected var lastSelected = 0

    fun submitList(src: List<M>, initSelectedIndex: Int) {
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