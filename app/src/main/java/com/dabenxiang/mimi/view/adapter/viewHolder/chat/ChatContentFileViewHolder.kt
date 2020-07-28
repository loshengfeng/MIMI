package com.dabenxiang.mimi.view.adapter.viewHolder.chat

import android.view.View
import android.widget.TextView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.adapter.ChatContentAdapter
import com.dabenxiang.mimi.view.adapter.viewHolder.chat.BaseChatContentViewHolder
import java.lang.StringBuilder

class ChatContentFileViewHolder(
        itemView: View,
        listener: ChatContentAdapter.EventListener
) : BaseChatContentViewHolder(itemView, listener) {
    private val txtMessage = itemView.findViewById(R.id.txt_message) as TextView

    init {
    }

    override fun updated() {
    }

    override fun updated(position: Int) {
        super.updated(position)
        val message = StringBuilder()
        message.append(data?.payload?.ext).append("\n").append(data?.payload?.content)
        txtMessage.text = message
    }
}