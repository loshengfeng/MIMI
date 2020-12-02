package com.dabenxiang.mimi.view.my_pages.pages.mimi_video

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MyCollectionVideoListener
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.adapter.viewHolder.*
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils

class MyCollectionMimiVideoAdapter(
        val context: Context,
        val funcItem: CollectionFuncItem,
        private val listener: MyCollectionVideoListener
) : PagingDataAdapter<PlayItem, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        const val PAYLOAD_UPDATE_LIKE = 0
        const val PAYLOAD_UPDATE_FAVORITE = 1
        const val PAYLOAD_UPDATE_FOLLOW = 2

        const val MIMI_VIDEO = 3
        const val VIEW_TYPE_AD = 4

        val diffCallback = object : DiffUtil.ItemCallback<PlayItem>() {
            override fun areItemsTheSame(
                    oldItem: PlayItem,
                    newItem: PlayItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                    oldItem: PlayItem,
                    newItem: PlayItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    var removedPosList = ArrayList<Int>()

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when (item?.playlistType?.toInt()) {
            PostType.AD.value -> VIEW_TYPE_AD
            else -> MIMI_VIDEO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            VIEW_TYPE_AD -> {
                AdHolder(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.item_ad, parent, false)
                )
            }
            else -> {
                return MyCollectionMIMIVideoViewHolder(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.item_my_follow_video, parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        item?.also {
            when (holder) {
                is AdHolder -> {
                    Glide.with(context).load(item.adItem?.href).into(holder.adImg)
                    holder.adImg.setOnClickListener {
                        GeneralUtils.openWebView(context, item.adItem?.target ?: "")
                    }
                }
                is MyCollectionMIMIVideoViewHolder -> {
                    holder.onBind(
                            it,
                            position,
                            listener,
                            funcItem
                    )
                }
            }
        }
    }
}