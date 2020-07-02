package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.holder.TopupProxyPayItem
import com.dabenxiang.mimi.view.listener.AdapterEventListener
import com.dabenxiang.mimi.view.topup.TopupProxyPayViewHolder
import timber.log.Timber

class TopupProxyPayAdapter(
    private val listener: AdapterEventListener<TopupProxyPayItem>
) : RecyclerView.Adapter<TopupProxyPayViewHolder>() {

    private var data: List<TopupProxyPayItem>? = null

    fun setDataSrc(itemList: List<TopupProxyPayItem>) {
        data = itemList
        updated()
    }

    private fun updated() {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopupProxyPayViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_topup_proxy_pay, parent, false)
        return TopupProxyPayViewHolder(view, listener)
    }

    override fun getItemCount(): Int {
        Timber.d("${TopupProxyPayAdapter::class.java.simpleName}_getItemCount")
        return data?.count() ?: 0
    }

    override fun onBindViewHolder(holderProxyPay: TopupProxyPayViewHolder, position: Int) {
        Timber.d("${TopupProxyPayViewHolder::class.java.simpleName}_onBindViewHolder_itemList: $data")
        val item = data?.get(position)
        holderProxyPay.bind(item)
    }
}