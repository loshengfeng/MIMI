package com.dabenxiang.mimi.view.adapter.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ChatListItem
import com.dabenxiang.mimi.view.adapter.ChatHistoryAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import java.text.SimpleDateFormat
import java.util.*

class ChatHistoryViewHolder(
        itemView: View,
        val listener: ChatHistoryAdapter.EventListener
) : BaseAnyViewHolder<ChatListItem>(itemView) {

    private val imgChatPhoto: ImageView = itemView.findViewById(R.id.imgChatPhoto) as ImageView
    private val imgIsNew: ImageView = itemView.findViewById(R.id.imgIsNew) as ImageView
    private val textName: TextView = itemView.findViewById(R.id.textName) as TextView
    private val textContent: TextView = itemView.findViewById(R.id.textContent) as TextView
    private val textDate: TextView = itemView.findViewById(R.id.textDate) as TextView
    private val btnChatHistory: ConstraintLayout = itemView.findViewById(R.id.btnChatHistory) as ConstraintLayout

    override fun updated(position: Int) {
        if (data?.lastReadTime == null || data?.lastMessageTime == null || data?.lastReadTime!!.after(data?.lastMessageTime!!)) {
            btnChatHistory.setBackgroundResource(R.drawable.btn_chat_history)
            imgIsNew.visibility = View.INVISIBLE
        } else {
            btnChatHistory.setBackgroundResource(R.drawable.btn_chat_history_new)
            imgIsNew.visibility = View.VISIBLE
        }

        btnChatHistory.setOnClickListener {
            data?.let { data -> listener.onClickListener(data, position) }
        }

        listener.onGetAttachment(data?.avatarAttachmentId, imgChatPhoto)

        textName.text = data?.name
        textContent.text = data?.message.toString()
        textDate.text = data?.lastMessageTime?.let { date -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date) }
    }

    override fun updated() {

    }
}