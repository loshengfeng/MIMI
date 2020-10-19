package com.dabenxiang.mimi.view.inviteviprecord

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ReferrerHistoryItem

class InviteVipRecordAdapter(
        private val listener: EventListener
) : PagedListAdapter<ReferrerHistoryItem, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<ReferrerHistoryItem>() {
            override fun areItemsTheSame(
                    oldItem: ReferrerHistoryItem,
                    newItem: ReferrerHistoryItem
            ): Boolean = oldItem == newItem

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                    oldItem: ReferrerHistoryItem,
                    newItem: ReferrerHistoryItem
            ): Boolean = oldItem == newItem
        }
    }

    interface EventListener {
        fun onClickListener(item: ReferrerHistoryItem, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteVipRecordViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return InviteVipRecordViewHolder(layoutInflater.inflate(R.layout.item_chat_history, parent, false), listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is InviteVipRecordViewHolder -> holder.bind(item, position)
        }
    }

    fun update(position: Int) {
        notifyItemChanged(position)
    }
}