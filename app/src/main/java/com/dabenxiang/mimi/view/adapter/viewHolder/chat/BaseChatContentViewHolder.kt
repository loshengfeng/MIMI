package com.dabenxiang.mimi.view.adapter.viewHolder.chat

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ChatContentItem
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.view.adapter.ChatContentAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import java.text.SimpleDateFormat
import java.util.*

open class BaseChatContentViewHolder(
    itemView: View,
    val listener: ChatContentAdapter.EventListener,
    val pref: Pref
) : BaseAnyViewHolder<ChatContentItem>(itemView) {
    private val txtTime = itemView.findViewById(R.id.txt_time) as TextView
    private val ivHead = itemView.findViewById(R.id.iv_head) as ImageView

    init {
    }

    override fun updated() {
    }

    override fun updated(position: Int) {
        super.updated(position)
        val avatarId = if (TextUtils.equals(pref.profileItem.userId.toString(), data?.username)) {
            pref.profileItem.avatarAttachmentId.toString()
        } else {
            listener.getSenderAvatar()
        }

        avatarId.let {
            LruCacheUtils.getLruArrayCache(avatarId)?.also { array ->
                val options: RequestOptions = RequestOptions()
                    .transform(MultiTransformation(CenterCrop(), CircleCrop()))
                    .placeholder(R.drawable.default_profile_picture)
                    .error(R.drawable.default_profile_picture)
                    .priority(Priority.NORMAL)

                Glide.with(App.self).asBitmap()
                    .load(array)
                    .apply(options)
                    .into(ivHead)
            } ?: takeIf { avatarId != "0" }?.run {
                listener.onGetAvatarAttachment(avatarId, position)
            }
        }
        txtTime.text = data?.payload?.sendTime?.let { date ->
            SimpleDateFormat(
                "YYYY-MM-dd HH:mm",
                Locale.getDefault()
            ).format(date)
        }
    }
}