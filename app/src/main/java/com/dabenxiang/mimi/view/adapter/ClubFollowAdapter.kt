package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.view.adapter.viewHolder.ClubFollowViewHolder

class ClubFollowAdapter(
    val listener: EventListener
) : PagedListAdapter<ClubFollowItem, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<ClubFollowItem>() {
            override fun areItemsTheSame(
                oldItem: ClubFollowItem,
                newItem: ClubFollowItem
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: ClubFollowItem,
                newItem: ClubFollowItem
            ): Boolean = oldItem == newItem
        }
    }

    interface EventListener {
        fun onDetail(item: ClubFollowItem)
        fun onGetAttachment(id: String, position: Int)
        fun onCancelFollow(clubId:Long)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ClubFollowViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_follow_club, parent, false),
            listener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is ClubFollowViewHolder -> holder.bind(item, position)
        }
    }

    fun update(position: Int) {
        notifyItemChanged(position)
    }
}