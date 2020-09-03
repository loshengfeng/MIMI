package com.dabenxiang.mimi.view.topup

import android.view.View
import com.dabenxiang.mimi.model.api.vo.AgentItem
import com.dabenxiang.mimi.view.adapter.TopUpAgentAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import kotlinx.android.synthetic.main.item_topup_proxy_pay.view.*
import timber.log.Timber

class TopUpProxyPayViewHolder(
    view: View,
    val listener: TopUpAgentAdapter.EventListener
) : BaseAnyViewHolder<AgentItem>(view) {
    private val ivPhoto = itemView.iv_photo
    private val tvSubtitle = itemView.tv_subtitle

    init {
        view.setOnClickListener { data?.let { item -> listener.onItemClick(view, item) } }
    }

    override fun updated() {
        Timber.d("${TopUpProxyPayViewHolder::class.java.simpleName}_updated")
        tvSubtitle.text = data?.merchantName

        listener.onGetAvatarAttachment(data?.avatarAttachmentId?.toLongOrNull(), ivPhoto)
    }
}