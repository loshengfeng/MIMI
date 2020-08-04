package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.home.viewholder.FilterTabHolder

class FilterTabAdapter(
    private val listener: FilterTabAdapterListener,
    private val isAdult: Boolean
) :
    BaseTabAdapter<String, FilterTabHolder>() {

    interface FilterTabAdapterListener {
        fun onSelectedFilterTab(recyclerView: RecyclerView, position: Int, keyword: String)
    }

    private var attachedRecyclerView: RecyclerView? = null

    private var newTabList: List<String>? = null

    private val holderListener = object : FilterTabHolder.FilterTabHolderListener {
        override fun onClickItemIndex(view: View, index: Int, isDisable: Boolean) {
            attachedRecyclerView?.takeUnless { isDisable }?.also {
                listener.onSelectedFilterTab(it, index, tabList?.get(index) ?: "")
            }
        }
    }

    fun updateList(src: List<String>?, initSelectedIndex: Int?) {
        initSelectedIndex?.also {
            lastSelected = it
        }
        newTabList = src

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterTabHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_filter, parent, false)
        return FilterTabHolder(
            view,
            holderListener,
            isAdult
        )
    }

    override fun onBindViewHolder(holder: FilterTabHolder, position: Int) {
        holder.bind(tabList?.get(position) ?: "", position)
        tabList?.get(position).let {
            newTabList == null || newTabList?.contains(it) == true
        }.also {
            holder.setTitleStyle(lastSelected == position, !it)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        attachedRecyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        attachedRecyclerView = null
    }
}