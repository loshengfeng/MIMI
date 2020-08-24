package com.dabenxiang.mimi.view.adapter.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ChatListItem
import com.dabenxiang.mimi.view.adapter.ChatHistoryAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class ChatHistoryViewHolder(
        itemView: View,
        val listener: ChatHistoryAdapter.EventListener
) : BaseAnyViewHolder<ChatListItem>(itemView) {

    private val imgChatPhoto: ImageView = itemView.findViewById(R.id.imgChatPhoto) as ImageView
    private val imgIsNew: ImageView = itemView.findViewById(R.id.imgIsNew) as ImageView
    private val textName: TextView = itemView.findViewById(R.id.textName) as TextView
    private val textContent: TextView = itemView.findViewById(R.id.textContent) as TextView
    private val textDate: TextView = itemView.findViewById(R.id.textDate) as TextView
    private val btnChatHistory: ConstraintLayout = itemView.findViewById(R.id.btnChatHistory) as ConstraintLayout

    init {
        Timber.d("@@init")
    }

    override fun updated(position: Int) {
        Timber.d("@@updated: $position")
        if (data?.lastReadTime == null || data?.lastMessageTime == null || data?.lastReadTime!!.after(data?.lastMessageTime!!)) {
            btnChatHistory.setBackgroundResource(R.drawable.btn_chat_history)
            imgIsNew.visibility = View.INVISIBLE
        } else {
            btnChatHistory.setBackgroundResource(R.drawable.btn_chat_history_new)
            imgIsNew.visibility = View.VISIBLE
        }

        btnChatHistory.setOnClickListener {
            Timber.d("@@setOnClickListener")
            data?.let { data ->
                Timber.d("@@setOnClickListener $data")
                listener.onClickListener(data) } }

        data?.avatarAttachmentId?.let {
            LruCacheUtils.getLruArrayCache(it.toString())?.also { array ->
                val options: RequestOptions = RequestOptions()
                        .transform(MultiTransformation(CenterCrop(), CircleCrop()))
                        .placeholder(R.drawable.default_profile_picture)
                        .error(R.drawable.default_profile_picture)
                        .priority(Priority.NORMAL)

                Glide.with(App.self)
                        .asBitmap()
                        .load(array)
                        .apply(options)
                        .into(imgChatPhoto)
            } ?: run {
                listener.onGetAttachment(it.toString(), position)
            }
        }

        textName.text = data?.name
        textContent.text = data?.message.toString()
        textDate.text = data?.lastMessageTime?.let { date -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date) }
    }

    override fun updated() {

    }
}