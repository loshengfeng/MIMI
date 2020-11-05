package com.dabenxiang.mimi.view.adapter.viewHolder.chat

import android.graphics.Bitmap
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.view.adapter.ChatContentAdapter
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.lang.IllegalArgumentException


class ChatContentImageViewHolder(
    itemView: View,
    listener: ChatContentAdapter.EventListener,
    pref: Pref
) : BaseChatContentViewHolder(itemView, listener, pref) {
    private val imgFile = itemView.findViewById(R.id.img_file) as ImageView
    private var fileArray: ByteArray? = null

    init {
        imgFile.setOnClickListener {
            var bitmapdata: ByteArray? = null
            var bitmap: Bitmap? = null
            try {
                bitmap = imgFile.drawable.toBitmap()
            } catch (e: IllegalArgumentException) {
                Timber.w("ImageView to drawable failed e = ${e.message}")
            }
            val stream = ByteArrayOutputStream()
            bitmapdata = if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                stream.toByteArray()
            } else {
                null
            }
            listener.onImageClick(bitmapdata)
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