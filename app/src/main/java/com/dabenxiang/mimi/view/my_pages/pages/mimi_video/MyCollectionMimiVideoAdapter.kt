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
import com.dabenxiang.mimi.view.adapter.viewHolder.AdHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.DeletedItemViewHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.MyCollectionMIMIVideoViewHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.MyCollectionShortVideoViewHolder
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.my_pages.base.MyPagesType
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.coroutines.CoroutineScope

class MyCollectionMimiVideoAdapter(
    val context: Context,
    val viewModelScope: CoroutineScope,
    val listener: MyCollectionVideoListener,
    val itemType: MyPagesType
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

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (item?.playlistType?.toInt() == PostType.AD.value) {
            VIEW_TYPE_AD
        } else if (itemType == MyPagesType.FAVORITE_MIMI_VIDEO) {
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
                            viewModelScope
                    )
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