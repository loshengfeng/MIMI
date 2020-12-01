package com.dabenxiang.mimi.view.mycollection.mimi_video

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MyFollowVideoListener
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.view.adapter.viewHolder.*
import com.dabenxiang.mimi.view.base.BaseViewHolder
import timber.log.Timber

class MyCollectionMimiVideoAdapter(
        val context: Context,
        private val listener: MyFollowVideoListener
) : PagingDataAdapter<PlayItem, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        const val PAYLOAD_UPDATE_LIKE = 0
        const val PAYLOAD_UPDATE_FAVORITE = 1
        const val PAYLOAD_UPDATE_FOLLOW = 2

        const val VIEW_TYPE_CLIP = 0
        const val VIEW_TYPE_PICTURE = 1
        const val VIEW_TYPE_TEXT = 2
        const val VIEW_TYPE_DELETED = 3
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

//    override fun getItemViewType(position: Int): Int {
//        val item = getItem(position)
//        return when (item?.type) {
//            PostType.VIDEO -> VIEW_TYPE_CLIP
//            PostType.IMAGE -> VIEW_TYPE_PICTURE
//            PostType.AD -> VIEW_TYPE_AD
//            else -> VIEW_TYPE_TEXT
//        }
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        Timber.d("neo = ${viewType}")
//        return when (viewType) {
//            VIEW_TYPE_AD -> {
//                AdHolder(
//                        LayoutInflater.from(parent.context)
//                                .inflate(R.layout.item_ad, parent, false)
//                )
//            }
        return MyFollowVideoViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_my_follow_video, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        item?.also {
            when (holder) {
//                is AdHolder -> {
//                    Glide.with(context).load(item.adItem?.href).into(holder.adImg)
//                    holder.adImg.setOnClickListener {
//                        GeneralUtils.openWebView(context, item.adItem?.target ?: "")
//                    }
//                }
                is MyFollowVideoViewHolder -> {
                    holder.onBind(
                            it,
                            null,
                            position,
                            listener
                    )
                }
            }
        }
    }
}