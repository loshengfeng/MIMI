package com.dabenxiang.mimi.view.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ChatContentItem
import com.dabenxiang.mimi.model.enums.ChatAdapterViewType
import com.dabenxiang.mimi.model.enums.ChatMessageType
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.view.adapter.viewHolder.chat.*
import org.koin.core.KoinComponent
import org.koin.core.inject

class ChatContentAdapter(
        private val listener: EventListener
) : PagedListAdapter<ChatContentItem, RecyclerView.ViewHolder>(diffCallback), KoinComponent {
    private val pref: Pref by inject()

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<ChatContentItem>() {
            override fun areItemsTheSame(
                    oldItem: ChatContentItem,
                    newItem: ChatContentItem
            ): Boolean = oldItem == newItem

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                    oldItem: ChatContentItem,
                    newItem: ChatContentItem
            ): Boolean = oldItem == newItem
        }
    }

    interface EventListener {
        fun onGetAttachment(id: String, position: Int)
        fun onImageClick(bitmap: Bitmap)
        fun onVideoClick(item: ChatContentItem?, position: Int)
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
                        ), listener
                )
            }
            ChatAdapterViewType.RECEIVER_IMAGE.ordinal -> {
                ChatContentImageViewHolder(
                        layoutInflater.inflate(
                                R.layout.item_chat_content_receiver_image,
                                parent,
                                false
                        ), listener
                )
            }
            ChatAdapterViewType.RECEIVER_BINARY.ordinal -> {
                ChatContentFileViewHolder(
                        layoutInflater.inflate(
                                R.layout.item_chat_content_receiver_file,
                                parent,
                                false
                        ), listener
                )
            }

            // Sender
            ChatAdapterViewType.SENDER_TEXT.ordinal -> {
                ChatContentTextViewHolder(
                        layoutInflater.inflate(
                                R.layout.item_chat_content_sender_text,
                                parent,
                                false
                        ), listener
                )
            }
            ChatAdapterViewType.SENDER_IMAGE.ordinal -> {
                ChatContentImageViewHolder(
                        layoutInflater.inflate(
                                R.layout.item_chat_content_sender_image,
                                parent,
                                false
                        ), listener
                )
            }
            ChatAdapterViewType.SENDER_BINARY.ordinal -> {
                ChatContentFileViewHolder(
                        layoutInflater.inflate(
                                R.layout.item_chat_content_sender_file,
                                parent,
                                false
                        ), listener
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
        val item = getItem(position)
        when (holder) {
            is ChatContentDateTitleViewHolder -> holder.bind(item)
            is ChatContentTextViewHolder -> holder.bind(item, position)
            is ChatContentImageViewHolder -> holder.bind(item, position)
            is ChatContentFileViewHolder -> holder.bind(item, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when {
            item?.dateTitle?.isNotEmpty() == true -> {
                ChatAdapterViewType.DATE_TITLE.ordinal
            }
            item?.username == pref.profileItem.userId.toString() -> {
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
}