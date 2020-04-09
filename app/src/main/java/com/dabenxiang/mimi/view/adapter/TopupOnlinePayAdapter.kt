package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.topup.TopupOnlinePayViewHolder
import timber.log.Timber

class TopupOnlinePayAdapter : RecyclerView.Adapter<TopupOnlinePayViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopupOnlinePayViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_topup_online_pay, parent, false)
        return TopupOnlinePayViewHolder(view)
    }

    override fun getItemCount(): Int {
        Timber.d("${TopupOnlinePayAdapter::class.java.simpleName}_getItemCount")
        return 4
    }

    override fun onBindViewHolder(holderOnlinePay: TopupOnlinePayViewHolder, position: Int) {
        Timber.d("${TopupOnlinePayAdapter::class.java.simpleName}_onBindViewHolder")
        holderOnlinePay.ivCheck.visibility = View.VISIBLE
        holderOnlinePay.tvToken.text = "300"
        holderOnlinePay.tvOriginalPrice.text = "¥ 50"
        holderOnlinePay.tvPrice.text = "¥ 50.00"
    }
}