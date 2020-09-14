package com.dabenxiang.mimi.view.adapter.viewHolder.chat

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.view.adapter.ChatContentAdapter
import java.io.ByteArrayOutputStream


class ChatContentImageViewHolder(
    itemView: View,
    listener: ChatContentAdapter.EventListener,
    pref: Pref
) : BaseChatContentViewHolder(itemView, listener, pref) {
    private val imgFile = itemView.findViewById(R.id.img_file) as ImageView
    private var fileArray: ByteArray? = null

    init {
        imgFile.setOnClickListener {
            val bitmap = (it as ImageView).drawable.toBitmap()
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val bitmapdata: ByteArray = stream.toByteArray()
            listener.onImageClick(bitmapdata)
        }
    }

    override fun updated() {
    }

    override fun updated(position: Int) {
        super.updated(position)
        listener.onGetAttachment(data?.payload?.content?.toLongOrNull(), imgFile, LoadImageType.CHAT_CONTENT)
    }
}