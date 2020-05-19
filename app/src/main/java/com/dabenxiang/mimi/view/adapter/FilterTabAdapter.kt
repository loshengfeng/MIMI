package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.home.FilterTabHolder

class FilterTabAdapter(private val listener: FilterTabAdapterListener, private val isAdult: Boolean) :
    RecyclerView.Adapter<FilterTabHolder>() {

    interface FilterTabAdapterListener {
        fun onSelectedFilterTab(recyclerView: RecyclerView, position: Int)
    }

    private var tabList: List<String>? = null
    private var lastPosition = 0
    private var attachedRecyclerView: RecyclerView? = null

    fun setTabList(src: List<String>, initSelectedPosition: Int) {
        lastPosition = initSelectedPosition
        tabList = src

        notifyDataSetChanged()
    }

    val size = tabList?.size ?: 0

    fun setLastSelectedPosition(position: Int) {
        val oldPosition = lastPosition
        lastPosition = position

        notifyItemChanged(oldPosition)
        notifyItemChanged(lastPosition)
    }

    private val holderListener = object : BaseIndexViewHolder.IndexViewHolderListener {
        override fun onClickItemIndex(view: View, position: Int) {
            attachedRecyclerView?.also {
                listener.onSelectedFilterTab(it, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return tabList?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterTabHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_filter, parent, false)
        return FilterTabHolder(view, holderListener, isAdult)
    }

    override fun onBindViewHolder(holder: FilterTabHolder, position: Int) {
        holder.bind(tabList?.get(position) ?: "", position)
        holder.setSelected(lastPosition == position)
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