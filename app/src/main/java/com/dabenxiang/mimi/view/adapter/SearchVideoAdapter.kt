package com.dabenxiang.mimi.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.FunctionType
import com.dabenxiang.mimi.view.adapter.viewHolder.SearchVideoViewHolder

class SearchVideoAdapter(
    private val listener: EventListener
) : PagedListAdapter<VideoItem, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<VideoItem>() {
            override fun areItemsTheSame(
                oldItem: VideoItem,
                newItem: VideoItem
            ): Boolean = oldItem == newItem

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: VideoItem,
                newItem: VideoItem
            ): Boolean = oldItem == newItem
        }
    }

    interface EventListener {
        fun onVideoClick(item: VideoItem)
        fun onFunctionClick(type: FunctionType, view: View, item: VideoItem)
        fun onChipClick(text: String)
        fun onAvatarDownload(view: ImageView, id: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return SearchVideoViewHolder(
                layoutInflater.inflate(
                    R.layout.item_favorite_normal,
                    parent,
                    false
                ), listener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is SearchVideoViewHolder -> holder.bind(item as VideoItem)
        }
    }
}