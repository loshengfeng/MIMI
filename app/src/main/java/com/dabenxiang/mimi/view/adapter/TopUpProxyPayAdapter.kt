package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.holder.TopUpProxyPayItem
import com.dabenxiang.mimi.view.listener.AdapterEventListener
import com.dabenxiang.mimi.view.topup.TopUpProxyPayViewHolder
import timber.log.Timber

class TopUpProxyPayAdapter(
    private val listener: AdapterEventListener<TopUpProxyPayItem>
) : RecyclerView.Adapter<TopUpProxyPayViewHolder>() {

    private var data: List<TopUpProxyPayItem>? = null

    fun setDataSrc(itemList: List<TopUpProxyPayItem>) {
        data = itemList
        updated()
    }

    private fun updated() {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopUpProxyPayViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_topup_proxy_pay, parent, false)
        return TopUpProxyPayViewHolder(view, listener)
    }

    override fun getItemCount(): Int {
        Timber.d("${TopUpProxyPayAdapter::class.java.simpleName}_getItemCount")
        return data?.count() ?: 0
    }

    override fun onBindViewHolder(holderProxyPay: TopUpProxyPayViewHolder, position: Int) {
        Timber.d("${TopUpProxyPayViewHolder::class.java.simpleName}_onBindViewHolder_itemList: $data")
        val item = data?.get(position)
        holderProxyPay.bind(item)
    }
}