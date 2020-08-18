package com.dabenxiang.mimi.view.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R

class OrderPagerAdapter: RecyclerView.Adapter<OrderPagerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderPagerViewHolder {
        return OrderPagerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_order_pager, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun onBindViewHolder(holder: OrderPagerViewHolder, position: Int) {
        holder.onBind(position)
    }

}