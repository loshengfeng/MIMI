package com.dabenxiang.mimi.view.adapter.viewHolder.chat

import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.view.adapter.ChatContentAdapter
import com.dabenxiang.mimi.widget.utility.LruCacheUtils


class ChatContentImageViewHolder(
    itemView: View,
    listener: ChatContentAdapter.EventListener,
    pref: Pref
) : BaseChatContentViewHolder(itemView, listener, pref) {
    private val imgFile = itemView.findViewById(R.id.img_file) as ImageView
    private var fileArray: ByteArray? = null

    init {
        imgFile.setOnClickListener {
            listener.onImageClick(fileArray)
        }
    }

    override fun updated() {
    }

    override fun updated(position: Int) {
        super.updated(position)
        Glide.with(App.self)
            .load(R.drawable.bg_gray_6_radius_16)
            .into(imgFile)

        data?.payload?.content?.let {
            LruCacheUtils.getLruArrayCache(it)?.also { byteArray ->
                val options: RequestOptions = RequestOptions()
                    .transform(CenterCrop(), RoundedCorners(16))
                    .placeholder(R.drawable.bg_gray_6_radius_16)
                    .error(R.drawable.bg_gray_6_radius_16)
                    .priority(Priority.NORMAL)
                Glide.with(App.self)
                    .asBitmap()
                    .load(byteArray)
                    .apply(options)
                    .into(imgFile)
                this.fileArray = byteArray
            } ?: run {
                listener.onGetAttachment(it, position)
            }
        }
    }
}