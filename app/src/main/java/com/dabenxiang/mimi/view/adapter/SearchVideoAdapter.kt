package com.dabenxiang.mimi.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.FunctionType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.adapter.viewHolder.AdHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.SearchVideoViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils

class SearchVideoAdapter(
    val context: Context,
    private val listener: EventListener,
    private val isAdult: Boolean = true
) : PagedListAdapter<VideoItem, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        const val VIEW_TYPE_VIDEO = 0
        const val VIEW_TYPE_AD = 1

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

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when (item?.type) {
            PostType.AD -> VIEW_TYPE_AD
            else -> VIEW_TYPE_VIDEO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_AD -> {
                AdHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_ad, parent, false)
                )
            }
            else -> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val viewId = when(isAdult) {
                    true -> R.layout.item_favorite_normal
                    else -> R.layout.item_general_normal
                }
                SearchVideoViewHolder(
                    layoutInflater.inflate(
                        viewId,
                        parent,
                        false
                    ), listener
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is AdHolder -> {
                Glide.with(context).load(item?.adItem?.href).into(holder.adImg)
                holder.adImg.setOnClickListener {
                    GeneralUtils.openWebView(context, item?.adItem?.target ?: "")
                }
            }
            is SearchVideoViewHolder -> holder.bind(item as VideoItem)
        }
    }

    interface EventListener {
        fun onVideoClick(item: VideoItem)
        fun onFunctionClick(type: FunctionType, view: View, item: VideoItem)
        fun onChipClick(text: String)
        fun onAvatarDownload(view: ImageView, id: String)
    }
}