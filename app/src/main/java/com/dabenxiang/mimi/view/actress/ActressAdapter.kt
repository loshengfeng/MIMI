package com.dabenxiang.mimi.view.actress

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ReferrerHistoryItem

class ActressAdapter(
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActressViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ActressViewHolder(layoutInflater.inflate(R.layout.item_invite_vip_record, parent, false), listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is ActressViewHolder -> holder.bind(item, position)
        }
    }

    fun update(position: Int) {
        notifyItemChanged(position)
    }
}