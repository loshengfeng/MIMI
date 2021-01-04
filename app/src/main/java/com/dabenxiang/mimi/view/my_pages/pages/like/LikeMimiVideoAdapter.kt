package com.dabenxiang.mimi.view.my_pages.pages.like

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
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.MyCollectionTabItemType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.adapter.viewHolder.*
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.my_pages.pages.mimi_video.MyCollectionMimiVideoAdapter
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber

class LikeMimiVideoAdapter(
    val context: Context,
    val viewModelScope: CoroutineScope,
    val listener: MyCollectionVideoListener,
    val itemType: MyCollectionTabItemType
) : PagingDataAdapter<PlayItem, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        const val PAYLOAD_UPDATE_LIKE = 0
        const val PAYLOAD_UPDATE_FAVORITE = 1
        const val PAYLOAD_UPDATE_FOLLOW = 2

        const val MIMI_VIDEO = 3
        const val SHORT_VIDEO = 5
        const val VIEW_TYPE_AD = 4
        const val VIEW_TYPE_DELETED = 6

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

    var changedPosList = HashMap<Long, VideoItem>()

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        val changedItem = changedPosList[item?.videoId]
        return if (changedItem != null && changedItem.like != true) {
            VIEW_TYPE_DELETED
        } else if (item?.playlistType?.toInt() == PostType.AD.value) {
            VIEW_TYPE_AD
        } else if (itemType == MyCollectionTabItemType.MIMI_VIDEO) {
            MIMI_VIDEO
        } else {
            SHORT_VIDEO
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
            MIMI_VIDEO -> {
                MyCollectionMIMIVideoViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_my_follow_video, parent, false)
                )
            }
            SHORT_VIDEO -> {
                MyCollectionShortVideoViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_my_follow_short_video, parent, false)
                )

            }
            else -> {
                DeletedItemViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_deleted, parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val item = getItem(position)
        val changedItem = changedPosList[item?.videoId]
        if (changedItem != null) {
            item?.like = changedItem.like
            item?.likeCount = changedItem.likeCount.toInt()
            item?.favorite = changedItem.favorite
            item?.favoriteCount = changedItem.favoriteCount?.toInt()
        }
        item?.also {
            when (holder) {
                is AdHolder -> {
                    Glide.with(context).load(item.adItem?.href).into(holder.adImg)
                    holder.adImg.setOnClickListener {
                        GeneralUtils.openWebView(context, item.adItem?.target ?: "")
                    }
                }
                is MyCollectionMIMIVideoViewHolder -> {
                    if (payloads.size == 1) {
                        when (payloads[0]) {
                            PAYLOAD_UPDATE_FAVORITE -> holder.updateFavorite(it)
                            PAYLOAD_UPDATE_LIKE -> holder.updateLike(it)
                        }
                    } else {
                        holder.onBind(
                            it,
                            position,
                            listener,
                            viewModelScope
                        )
                    }
                }
                is MyCollectionShortVideoViewHolder -> {
                    holder.onBind(
                        it,
                        position,
                        listener,
                        viewModelScope
                    )
                }
            }
        }
    }
}