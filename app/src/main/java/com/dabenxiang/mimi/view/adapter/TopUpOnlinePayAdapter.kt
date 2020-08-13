package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.OrderingPackageItem
import com.dabenxiang.mimi.view.topup.TopUpOnlinePayViewHolder

class TopUpOnlinePayAdapter(val context: Context) :
    RecyclerView.Adapter<TopUpOnlinePayViewHolder>() {

    private var selectItem: OrderingPackageItem? = null

    private var orderingPackageItems: ArrayList<OrderingPackageItem>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopUpOnlinePayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_topup_online_pay, parent, false)
        return TopUpOnlinePayViewHolder(view)
    }

    override fun getItemCount(): Int {
        return orderingPackageItems?.size ?: 0
    }

    override fun onBindViewHolder(holder: TopUpOnlinePayViewHolder, position: Int) {
        val item = orderingPackageItems?.get(position)
        holder.tvPackageName.text = item?.name
        holder.listPrice.text = item?.listPrice.toString()
        holder.price.text = StringBuilder("¥ ").append(item?.price).toString()
        holder.price.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG

        if (selectItem != null && selectItem?.id == item?.id) {
            holder.ivCheck.visibility = View.VISIBLE
            holder.orderPackageLayout.background = context.getDrawable(
                R.drawable.bg_red_1_stroke_1_radius_8
            )
        } else {
            holder.ivCheck.visibility = View.INVISIBLE
            holder.orderPackageLayout.background = context.getDrawable(
                R.drawable.bg_gray_7_stroke_1_radius_8
            )
        }

        holder.orderPackageLayout.setOnClickListener {
            selectItem = item
            notifyDataSetChanged()
        }
    }

    fun setupData(data: ArrayList<OrderingPackageItem>) {
        orderingPackageItems = data
    }

    fun clearSelectItem() {
        selectItem = null
    }

    fun getSelectItem(): OrderingPackageItem? {
        return selectItem
    }
}