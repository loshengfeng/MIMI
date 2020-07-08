package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.holder.TopUpOnlinePayItem
import com.dabenxiang.mimi.view.listener.AdapterEventListener
import com.dabenxiang.mimi.view.topup.TopUpOnlinePayViewHolder

class TopUpOnlinePayAdapter(
    private val listener: AdapterEventListener<TopUpOnlinePayItem>
) : RecyclerView.Adapter<TopUpOnlinePayViewHolder>() {

    private var data: List<TopUpOnlinePayItem>? = null

    fun setDataSrc(itemList: List<TopUpOnlinePayItem>) {
        data = itemList
        updated()
    }

    private fun updated() {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopUpOnlinePayViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_topup_online_pay, parent, false)
        return TopUpOnlinePayViewHolder(view, listener)
    }

    override fun getItemCount(): Int {
        return data?.count() ?: 0
    }

    override fun onBindViewHolder(holderOnlinePay: TopUpOnlinePayViewHolder, position: Int) {
        val item = data?.get(position)
        holderOnlinePay.bind(item)
    }
}