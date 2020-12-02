package com.dabenxiang.mimi.view.my_pages.follow.follow_list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.BaseItemListener
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.view.adapter.viewHolder.*
import com.dabenxiang.mimi.view.base.BaseViewHolder

class ClubFollowPeopleAdapter(
        val context: Context,
        private val listener: BaseItemListener
) : PagingDataAdapter<ClubFollowItem, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<ClubFollowItem>() {
            override fun areItemsTheSame(
                    oldItem: ClubFollowItem,
                    newItem: ClubFollowItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                    oldItem: ClubFollowItem,
                    newItem: ClubFollowItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    var removedPosList = ArrayList<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return ClubFollowViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_follow_club, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        item?.also {

            (holder as ClubFollowViewHolder)
//                    holder.pictureRecycler.tag = position
            holder.onBind(
                    it, listener
            )

        }
    }
}