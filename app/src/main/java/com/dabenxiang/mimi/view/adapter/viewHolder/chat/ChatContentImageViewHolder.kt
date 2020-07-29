package com.dabenxiang.mimi.view.adapter.viewHolder.chat

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.adapter.ChatContentAdapter
import com.dabenxiang.mimi.widget.utility.LruCacheUtils


class ChatContentImageViewHolder(
        itemView: View,
        listener: ChatContentAdapter.EventListener
) : BaseChatContentViewHolder(itemView, listener) {
    private val imgFile = itemView.findViewById(R.id.img_file) as ImageView
    private var bitmap: Bitmap? = null

    init {
        imgFile.setOnClickListener {
            bitmap?.let { listener.onImageClick(it) }
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
            LruCacheUtils.getLruCache(it)?.also { bitmap ->
                val options: RequestOptions = RequestOptions()
                        .transform(CenterCrop(), RoundedCorners(16))
                        .placeholder(R.drawable.bg_gray_6_radius_16)
                        .error(R.drawable.bg_gray_6_radius_16)
                        .priority(Priority.NORMAL)

                Glide.with(App.self)
                        .load(bitmap)
                        .apply(options)
                        .into(imgFile)
                this.bitmap = bitmap
            } ?: run {
                listener.onGetAttachment(it, position)
            }
        }
    }
}