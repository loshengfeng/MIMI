package com.dabenxiang.mimi.view.adapter.viewHolder.chat

import android.view.View
import android.widget.TextView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ChatContentItem
import com.dabenxiang.mimi.view.adapter.ChatContentAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder

class ChatContentDateTitleViewHolder(
    itemView: View,
    val listener: ChatContentAdapter.EventListener
) : BaseAnyViewHolder<ChatContentItem>(itemView) {

    private val tvDate = itemView.findViewById(R.id.txt_date) as TextView

    override fun updated() {
        tvDate.text = data?.dateTitle
    }
}