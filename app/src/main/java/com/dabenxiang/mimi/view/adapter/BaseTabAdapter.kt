package com.dabenxiang.mimi.view.adapter

import androidx.recyclerview.widget.RecyclerView

abstract class BaseTabAdapter<M : Any, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    protected var tabList: List<M>? = null
    protected var lastSelected: Int? = null

    fun submitList(src: List<M>, initSelectedIndex: Int?) {
        initSelectedIndex?.also {
            lastSelected = it
        }
        tabList = src

        notifyDataSetChanged()
    }

    fun setLastSelectedIndex(index: Int?) {
        index?.also { i ->
            val oldSelected = lastSelected
            lastSelected = i

            oldSelected?.takeIf { it >= 0 }?.also { notifyItemChanged(it) }
            lastSelected?.takeIf { it >= 0 }?.also { notifyItemChanged(it) }
        }
    }

    override fun getItemCount(): Int {
        return tabList?.size ?: 0
    }
}