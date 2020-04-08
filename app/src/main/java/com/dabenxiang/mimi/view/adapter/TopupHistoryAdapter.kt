package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.topuphistory.TopupHistoryViewHolder
import timber.log.Timber

class TopupHistoryAdapter : RecyclerView.Adapter<TopupHistoryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopupHistoryViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_topup_history, parent, false)
        return TopupHistoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        Timber.d("${TopupHistoryAdapter::class.java.simpleName}_getItemCount")
        return 10
    }

    override fun onBindViewHolder(holder: TopupHistoryViewHolder, position: Int) {
        Timber.d("${TopupHistoryAdapter::class.java.simpleName}_onBindViewHolder")
        holder.tvAccount.text = "1234***890"
        holder.tvTime.text = "2020-05-05 14:00"
        holder.tvMoney.text = "3000+300"
        holder.tvMoney2.text = "¥ 50.00"
    }

}