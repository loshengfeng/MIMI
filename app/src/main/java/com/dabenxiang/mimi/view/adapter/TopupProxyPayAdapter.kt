package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.topup.TopupProxyPayViewHolder
import timber.log.Timber

class TopupProxyPayAdapter : RecyclerView.Adapter<TopupProxyPayViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopupProxyPayViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_topup_proxy_pay, parent, false)
        return TopupProxyPayViewHolder(view)
    }

    override fun getItemCount(): Int {
        Timber.d("${TopupProxyPayAdapter::class.java.simpleName}_getItemCount")
        return 4
    }

    override fun onBindViewHolder(holderProxyPay: TopupProxyPayViewHolder, position: Int) {
        Timber.d("${TopupProxyPayAdapter::class.java.simpleName}_onBindViewHolder")
//        holderProxyPay.ivPhoto.drawable(App.applicationContext().getDrawable(R.drawable.ico_checked))
        holderProxyPay.tvTitle.text = "火热代理"
        holderProxyPay.tvSubtitle.text = "密密"
    }
}