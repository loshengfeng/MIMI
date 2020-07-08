package com.dabenxiang.mimi.view.topup

import android.view.View
import com.dabenxiang.mimi.model.holder.TopUpOnlinePayItem
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import com.dabenxiang.mimi.view.listener.AdapterEventListener
import kotlinx.android.synthetic.main.item_topup_online_pay.view.*
import timber.log.Timber

class TopUpOnlinePayViewHolder(
    view: View,
    listener: AdapterEventListener<TopUpOnlinePayItem>
) : BaseAnyViewHolder<TopUpOnlinePayItem>(view) {
    private val ivCheck = itemView.iv_check!!
    private val tvToken = itemView.tv_token!!
    private val tvOriginalPrice = itemView.tv_original_price!!
    private val tvPrice = itemView.tv_price!!

    init {
        view.setOnClickListener { data?.let { it -> listener.onItemClick(view, it) } }
    }

    override fun updated() {
        Timber.d("${TopUpOnlinePayViewHolder::class.java.simpleName}_updated")
        ivCheck.visibility = View.VISIBLE
        tvToken.text = data?.token
        tvPrice.text = data?.price
        tvOriginalPrice.text = data?.originalPrice
    }
}