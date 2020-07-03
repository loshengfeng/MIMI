package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import timber.log.Timber

class ChatHistoryAdapter(
    private val chatHistory: ArrayList<FakeChatHistory>,
    private val onClickListener: View.OnClickListener?
) : RecyclerView.Adapter<ChatHistoryAdapter.ChatHistoryViewHolder>() {

    data class FakeChatHistory(
        val id: String,
        val name: String,
        val message: String,
        val imgUrl: String,
        val lastDate: String,
        val isRead: Boolean
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHistoryViewHolder {
        return ChatHistoryViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_chat_history, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return chatHistory.size
    }

    override fun onBindViewHolder(holder: ChatHistoryViewHolder, position: Int) {
        val history = chatHistory[position]

        if (history.isRead) {
            holder.btnChatHistory.setBackgroundResource(R.drawable.btn_chat_history)
            holder.imgIsNew.visibility = View.INVISIBLE
        } else {
            holder.btnChatHistory.setBackgroundResource(R.drawable.btn_chat_history_new)
            holder.imgIsNew.visibility = View.VISIBLE
        }

        Timber.d("${ChatHistoryAdapter::class.java.simpleName}_url: ${history.imgUrl}")

        Glide.with(holder.imgChatPhoto.context)
            .load(history.imgUrl)
            .into(holder.imgChatPhoto)

        holder.textName.text = history.name
        holder.textContent.text = history.message
        holder.textDate.text = history.lastDate
        holder.btnChatHistory.setOnClickListener(onClickListener)
    }

    class ChatHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgChatPhoto: ImageView = itemView.findViewById(R.id.imgChatPhoto) as ImageView
        val imgIsNew: ImageView = itemView.findViewById(R.id.imgIsNew) as ImageView
        val textName: TextView = itemView.findViewById(R.id.textName) as TextView
        val textContent: TextView = itemView.findViewById(R.id.textContent) as TextView
        val textDate: TextView = itemView.findViewById(R.id.textDate) as TextView
        val btnChatHistory: ConstraintLayout =
            itemView.findViewById(R.id.btnChatHistory) as ConstraintLayout
    }
}