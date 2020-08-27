package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.view.adapter.viewHolder.ClubFollowViewHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.DeletedItemViewHolder

class ClubFollowAdapter(
    val listener: EventListener
) : PagingDataAdapter<ClubFollowItem, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private const val VIEW_TYPE_NORMAL = 0
        private const val VIEW_TYPE_DELETED = 1

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
        fun onCancelFollow(clubId: Long, position: Int)
    }

    var removedPosList = ArrayList<Int>()

    override fun getItemViewType(position: Int): Int {
        return if (removedPosList.contains(position)) {
            VIEW_TYPE_DELETED
        } else {
            VIEW_TYPE_NORMAL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_NORMAL -> ClubFollowViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_follow_club, parent, false), listener
            )
            else -> DeletedItemViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_deleted, parent, false)
            )
        }
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