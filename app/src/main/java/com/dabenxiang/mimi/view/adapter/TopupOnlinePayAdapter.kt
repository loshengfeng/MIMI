package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.holder.TopupOnlinePayItem
import com.dabenxiang.mimi.view.listener.AdapterEventListener
import com.dabenxiang.mimi.view.topup.TopupOnlinePayViewHolder

class TopupOnlinePayAdapter(
    private val listener: AdapterEventListener<TopupOnlinePayItem>
) : RecyclerView.Adapter<TopupOnlinePayViewHolder>() {

    private var data: List<TopupOnlinePayItem>? = null

    fun setDataSrc(itemList: List<TopupOnlinePayItem>) {
        data = itemList
        updated()
    }

    private fun updated() {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopupOnlinePayViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_topup_online_pay, parent, false)
        return TopupOnlinePayViewHolder(view, listener)
    }

    override fun getItemCount(): Int {
        return data?.count() ?: 0
    }

    override fun onBindViewHolder(holderOnlinePay: TopupOnlinePayViewHolder, position: Int) {
        val item = data?.get(position)
        holderOnlinePay.bind(item)
    }
}