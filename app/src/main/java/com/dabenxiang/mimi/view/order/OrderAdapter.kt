package com.dabenxiang.mimi.view.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.view.adapter.viewHolder.OrderViewHolder
import com.dabenxiang.mimi.view.base.BaseViewHolder

class OrderAdapter(private val orderFuncItem: OrderFuncItem?) :
    PagedListAdapter<OrderItem, BaseViewHolder>(COMPARATOR) {
    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<OrderItem>() {
            override fun areItemsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return OrderViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (holder) {
            is OrderViewHolder -> holder.bind(getItem(position), orderFuncItem)
        }
    }
}