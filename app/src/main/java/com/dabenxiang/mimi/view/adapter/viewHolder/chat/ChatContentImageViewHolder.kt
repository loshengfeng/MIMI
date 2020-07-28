package com.dabenxiang.mimi.view.adapter.viewHolder.chat

import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.adapter.ChatContentAdapter
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import timber.log.Timber


class ChatContentImageViewHolder(
        itemView: View,
        listener: ChatContentAdapter.EventListener
) : BaseChatContentViewHolder(itemView, listener) {
    val imgFile = itemView.findViewById(R.id.img_file) as ImageView

    init {
    }

    override fun updated() {
    }

    override fun updated(position: Int) {
        super.updated(position)
        data?.payload?.content?.let {
            Timber.d("neo, content = ${it}")
            LruCacheUtils.getLruCache(it)?.also { bitmap ->
                val options: RequestOptions = RequestOptions()
                        .transform(MultiTransformation(CenterCrop(), CircleCrop()))
                        .placeholder(R.drawable.default_profile_picture)
                        .error(R.drawable.default_profile_picture)
                        .priority(Priority.NORMAL)

                Glide.with(App.self)
                        .load(bitmap)
                        .transform(CenterCrop(), RoundedCorners(16))
                        .into(imgFile)
            } ?: run {
                Timber.d("neo,conten = ${it}")
                listener.onGetAttachment(it, position)
            }
        }
    }
}