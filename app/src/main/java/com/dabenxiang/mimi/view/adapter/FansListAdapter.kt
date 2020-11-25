package com.dabenxiang.mimi.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ChatListItem
import com.dabenxiang.mimi.model.api.vo.error.FansItem
import com.dabenxiang.mimi.view.adapter.viewHolder.ChatHistoryViewHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.FansViewHolder

class FansListAdapter(
        private val listener: EventListener
) : PagedListAdapter<FansItem, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<FansItem>() {
            override fun areItemsTheSame(
                    oldItem: FansItem,
                    newItem: FansItem
            ): Boolean = oldItem == newItem

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                    oldItem: FansItem,
                    newItem: FansItem
            ): Boolean = oldItem == newItem
        }
    }

    interface EventListener {
        fun onClickListener(item: FansItem, position: Int)
        fun onGetAttachment(id: Long?, view: ImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FansViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return FansViewHolder(layoutInflater.inflate(R.layout.item_fans, parent, false), listener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is FansViewHolder -> holder.bind(item, position)
        }
    }

    fun update(position: Int) {
        notifyItemChanged(position)
    }
}