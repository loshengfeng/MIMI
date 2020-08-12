package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberFollowItem
import com.dabenxiang.mimi.view.adapter.viewHolder.MemberFollowViewHolder

class MemberFollowAdapter(
    private val listener: EventListener
) : PagedListAdapter<MemberFollowItem, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<MemberFollowItem>() {
            override fun areItemsTheSame(
                oldItem: MemberFollowItem,
                newItem: MemberFollowItem
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: MemberFollowItem,
                newItem: MemberFollowItem
            ): Boolean = oldItem == newItem
        }
    }

    interface EventListener {
        fun onDetail(item: MemberFollowItem)
        fun onGetAttachment(id: String, position: Int)
        fun onCancelFollow(userId:Long)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MemberFollowViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_follow_member, parent, false),
            listener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is MemberFollowViewHolder -> holder.bind(item, position)
        }
    }

    fun update(position: Int) {
        notifyItemChanged(position)
    }
}