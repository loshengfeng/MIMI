package com.dabenxiang.mimi.view.adapter.viewHolder.chat

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.view.adapter.ChatContentAdapter

class ChatContentImageViewHolder(
    itemView: View,
    listener: ChatContentAdapter.EventListener,
    pref: Pref
) : BaseChatContentViewHolder(itemView, listener, pref) {
    private val imgFile = itemView.findViewById(R.id.img_file) as ImageView
    private var fileArray: ByteArray? = null

    init {
        imgFile.setOnClickListener {
            listener.onImageClick(data)
        }
    }

    override fun updated() {
    }

    override fun updated(position: Int) {
        super.updated(position)
        if (!TextUtils.isEmpty(data?.cacheImagePath)) {
            data?.cacheImagePath?.let { listener.onGetAttachment(it, imgFile, LoadImageType.CHAT_CONTENT) }
        } else {
            listener.onGetAttachment(data?.payload?.content?.toLongOrNull(), imgFile, LoadImageType.CHAT_CONTENT)
        }
    }
}