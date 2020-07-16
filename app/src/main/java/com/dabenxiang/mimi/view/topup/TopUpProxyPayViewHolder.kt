package com.dabenxiang.mimi.view.topup

import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.AgentItem
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import com.dabenxiang.mimi.view.listener.AdapterEventListener
import kotlinx.android.synthetic.main.item_topup_proxy_pay.view.*
import timber.log.Timber

class TopUpProxyPayViewHolder(
    view: View,
    listener: AdapterEventListener<AgentItem>
) : BaseAnyViewHolder<AgentItem>(view) {
    private val ivPhoto = itemView.iv_photo!!
    private val tvTitle = itemView.tv_title!!
    private val tvSubtitle = itemView.tv_subtitle!!

    init {
        view.setOnClickListener { data?.let { it -> listener.onItemClick(view, it) } }
    }

    override fun updated() {
        Timber.d("${TopUpProxyPayViewHolder::class.java.simpleName}_updated")
        ivPhoto.setImageResource(R.drawable.ico_default_photo)
        tvTitle.text = data?.merchantName
    }
}