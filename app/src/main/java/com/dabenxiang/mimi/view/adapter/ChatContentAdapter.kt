package com.dabenxiang.mimi.view.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ChatContentItem
import com.dabenxiang.mimi.model.enums.ChatAdapterViewType
import com.dabenxiang.mimi.model.enums.ChatMessageType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.view.adapter.viewHolder.chat.ChatContentDateTitleViewHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.chat.ChatContentFileViewHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.chat.ChatContentImageViewHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.chat.ChatContentTextViewHolder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatContentAdapter(
    private val pref: Pref,
    private val listener: EventListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var data = ArrayList<ChatContentItem>()

    interface EventListener {
        fun onGetAttachment(id: Long?, view:ImageView, type:LoadImageType)
        fun onGetAttachment(filePath: String, view:ImageView, type:LoadImageType)
        fun onImageClick(imageArray: ByteArray?)
        fun onVideoClick(item: ChatContentItem?, position: Int)
        fun getSenderAvatar(): String
        fun onImageClick(data: ChatContentItem?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ChatAdapterViewType.DATE_TITLE.ordinal -> {
                ChatContentDateTitleViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_chat_content_date,
                        parent,
                        false
                    ), listener
                )
            }

            // Receiver
            ChatAdapterViewType.RECEIVER_TEXT.ordinal -> {
                ChatContentTextViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_chat_content_receiver_text,
                        parent,
                        false
                    ), listener, pref
                )
            }
            ChatAdapterViewType.RECEIVER_IMAGE.ordinal -> {
                ChatContentImageViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_chat_content_receiver_image,
                        parent,
                        false
                    ), listener, pref
                )
            }
            ChatAdapterViewType.RECEIVER_BINARY.ordinal -> {
                ChatContentFileViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_chat_content_receiver_file,
                        parent,
                        false
                    ), listener, pref
                )
            }

            // Sender
            ChatAdapterViewType.SENDER_TEXT.ordinal -> {
                ChatContentTextViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_chat_content_sender_text,
                        parent,
                        false
                    ), listener, pref
                )
            }
            ChatAdapterViewType.SENDER_IMAGE.ordinal -> {
                ChatContentImageViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_chat_content_sender_image,
                        parent,
                        false
                    ), listener, pref
                )
            }
            ChatAdapterViewType.SENDER_BINARY.ordinal -> {
                ChatContentFileViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_chat_content_sender_file,
                        parent,
                        false
                    ), listener, pref
                )
            }
            else -> {
                ChatContentDateTitleViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_chat_content_receiver_text,
                        parent,
                        false
                    ), listener
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        when (holder) {
            is ChatContentDateTitleViewHolder -> holder.bind(item)
            is ChatContentTextViewHolder -> holder.bind(item, position)
            is ChatContentImageViewHolder -> holder.bind(item, position)
            is ChatContentFileViewHolder -> holder.bind(item, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = data[position]
        return when {
            item.dateTitle?.isNotEmpty() == true -> {
                ChatAdapterViewType.DATE_TITLE.ordinal
            }
            item.username == pref.profileItem.userId.toString() -> {
                when (item.payload?.type) {
                    ChatMessageType.TEXT.ordinal -> {
                        ChatAdapterViewType.RECEIVER_TEXT.ordinal
                    }
                    ChatMessageType.IMAGE.ordinal -> {
                        ChatAdapterViewType.RECEIVER_IMAGE.ordinal
                    }
                    ChatMessageType.BINARY.ordinal -> {
                        ChatAdapterViewType.RECEIVER_BINARY.ordinal
                    }
                    else -> {
                        ChatAdapterViewType.RECEIVER_TEXT.ordinal
                    }
                }
            }
            else -> {
                when (item?.payload?.type) {
                    ChatMessageType.TEXT.ordinal -> {
                        ChatAdapterViewType.SENDER_TEXT.ordinal
                    }
                    ChatMessageType.IMAGE.ordinal -> {
                        ChatAdapterViewType.SENDER_IMAGE.ordinal
                    }
                    ChatMessageType.BINARY.ordinal -> {
                        ChatAdapterViewType.SENDER_BINARY.ordinal
                    }
                    else -> {
                        ChatAdapterViewType.SENDER_TEXT.ordinal
                    }
                }
            }
        }
    }

    fun update(position: Int) {
        notifyItemChanged(position)
    }

    fun setData(data: ArrayList<ChatContentItem>) {
        if (this.data.size > 0) {
            this.data.removeAt(this.data.size - 1)
        }
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    fun insertItem(item: ChatContentItem, index: Int = 0) {
        // 判斷需不需要先加上時間 Title
        if (this.data.size > 0) {
            val lastItemDate = this.data[0].payload?.sendTime?.let { time ->
                SimpleDateFormat(
                    "YYYY-MM-dd",
                    Locale.getDefault()
                ).format(time)
            }
            val currentItemDate = item.payload?.sendTime?.let { time ->
                SimpleDateFormat(
                    "YYYY-MM-dd",
                    Locale.getDefault()
                ).format(time)
            }
            if (currentItemDate != null && !TextUtils.equals(lastItemDate, currentItemDate)) {
                this.data.add(index, ChatContentItem(dateTitle = currentItemDate))
            }
        }

        this.data.add(index, item)
        notifyDataSetChanged()
    }

    /**
     * 根據暫存的 cache map 去更新上一次德暫存 item
     */
    fun updateCacheData(item: ChatContentItem, uploadCache: HashMap<String, Int>) {
        var updateIndex = -1
        for (i: Int in 0 until this.data.size) {
            if (this.data[i].mediaHashCode == uploadCache[item.payload?.content] ?: -1) {
                updateIndex = i
                break
            }
        }
        if (updateIndex != -1) {
            this.data[updateIndex] = item
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}