package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.BaseItemListener
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.view.adapter.viewHolder.ClubFollowViewHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.DeletedItemViewHolder

class FollowClubAdapter(
    val listener: BaseItemListener
) : PagingDataAdapter<ClubFollowItem, RecyclerView.ViewHolder>(diffCallback) {
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

    var removedPosList = ArrayList<Int>()

    override fun getItemViewType(position: Int): Int {
        return if (removedPosList.contains(position)) {
            R.layout.item_deleted
        } else {
            R.layout.item_follow_club
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.item_follow_club -> ClubFollowViewHolder(view)
            else -> DeletedItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        item?.also {
            when (holder) {
                is ClubFollowViewHolder -> {
//                    holder.onBind(
//                        it,
//                        listener
//                    )
                }
            }
        }
    }
}