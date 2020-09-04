package com.dabenxiang.mimi.view.adapter.viewHolder.chat

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ChatContentItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.view.adapter.ChatContentAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
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
        val avatarId: Long?
        val type: LoadImageType
        if (TextUtils.equals(pref.profileItem.userId.toString(), data?.username)) {
            avatarId = pref.profileItem.avatarAttachmentId
            type = LoadImageType.AVATAR
        } else {
            avatarId = listener.getSenderAvatar().toLongOrNull()
            type =  LoadImageType.AVATAR_CS
        }
        listener.onGetAttachment(avatarId, ivHead, type)

        txtTime.text = data?.payload?.sendTime?.let { date ->
            SimpleDateFormat(
                "YYYY-MM-dd HH:mm",
                Locale.getDefault()
            ).format(date)
        }
    }
}