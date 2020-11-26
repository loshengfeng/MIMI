package com.dabenxiang.mimi.view.player

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.model.vo.BaseVideoItem
import com.dabenxiang.mimi.view.adapter.viewHolder.GarbageViewHolder

class GuessLikeVideoAdapter(
    val listener: OnGarbageItemClick
) : PagedListAdapter<BaseVideoItem, RecyclerView.ViewHolder>(diffCallback) {

    companion object {

        private val diffCallback = object : DiffUtil.ItemCallback<BaseVideoItem>() {
            override fun areItemsTheSame(
                oldItem: BaseVideoItem,
                newItem: BaseVideoItem
            ): Boolean = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: BaseVideoItem,
                newItem: BaseVideoItem
            ): Boolean = oldItem == newItem
        }
    }

    interface OnGarbageItemClick {
        fun onStatisticsDetail(baseVideoItem: BaseVideoItem)
        fun onTagClick(tag: String)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when(holder) {
            is GarbageViewHolder -> holder.bind(item as BaseVideoItem.Video, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return GarbageViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.nested_garbage_video_item, parent, false), listener)
    }
}