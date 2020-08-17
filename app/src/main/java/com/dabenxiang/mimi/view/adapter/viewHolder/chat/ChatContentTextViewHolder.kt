package com.dabenxiang.mimi.view.adapter.viewHolder.chat

import android.view.View
import android.widget.TextView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.enums.VideoDownloadStatusType
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.view.adapter.ChatContentAdapter

class ChatContentTextViewHolder(
    itemView: View,
    listener: ChatContentAdapter.EventListener,
    pref: Pref
) : BaseChatContentViewHolder(itemView, listener, pref) {
    private val txtMessage = itemView.findViewById(R.id.txt_message) as TextView

    override fun updated() {
    }

    override fun updated(position: Int) {
        super.updated(position)
        txtMessage.text =
            if (data?.downloadStatus == VideoDownloadStatusType.UPLOADING) itemView.context.getString(
                R.string.chat_content_file_uploading
            ) else data?.payload?.content
    }
}