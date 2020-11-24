package com.dabenxiang.mimi.view.club.latest

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.adapter.viewHolder.*
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils

class ClubLatestAdapter(
        val context: Context,
        private val adultListener: AdultListener,
        private var mTag: String = "",
        private val memberPostFuncItem: MemberPostFuncItem = MemberPostFuncItem(),
        private val isClipList: Boolean = false
) : PagingDataAdapter<StatisticsItem, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        const val PAYLOAD_UPDATE_LIKE = 0
        const val PAYLOAD_UPDATE_FOLLOW = 1
        const val VIEW_TYPE_CLIP = 0
        const val VIEW_TYPE_PICTURE = 1
        const val VIEW_TYPE_TEXT = 2
        const val VIEW_TYPE_AD = 3
        const val VIEW_TYPE_DELETED = 4

        private val diffCallback = object : DiffUtil.ItemCallback<StatisticsItem>() {
            override fun areItemsTheSame(
                    oldItem: StatisticsItem,
                    newItem: StatisticsItem
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                    oldItem: StatisticsItem,
                    newItem: StatisticsItem
            ): Boolean = oldItem == newItem
        }
    }

    val viewHolderMap = hashMapOf<Int, RecyclerView.ViewHolder>()

    var removedPosList = ArrayList<Int>()

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (removedPosList.contains(position)) {
            VIEW_TYPE_DELETED
        } else {
            when (item?.type) {
                PostType.VIDEO -> VIEW_TYPE_CLIP
                PostType.IMAGE -> VIEW_TYPE_PICTURE
                PostType.AD -> VIEW_TYPE_AD
                else -> VIEW_TYPE_TEXT
            }
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
            VIEW_TYPE_CLIP -> {
                ClipPostHolder(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.item_clip_post, parent, false)
                )
            }
            VIEW_TYPE_PICTURE -> {
                PicturePostHolder(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.item_picture_post, parent, false)
                )
            }
            VIEW_TYPE_TEXT -> {
                TextPostHolder(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.item_text_post, parent, false)
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

        viewHolderMap[position] = holder
        val item = getItem(position)
        when (holder) {
            is AdHolder -> {
                Glide.with(context).load(item?.adItem?.href).into(holder.adImg)
                holder.adImg.setOnClickListener {
                    GeneralUtils.openWebView(context, item?.adItem?.target ?: "")
                }
            }
//            is ClipPostHolder -> {
//                item?.also {
//                    holder.onBind(
//                            item,
//                            null,
//                            position,
//                            adultListener,
//                            mTag,
//                            memberPostFuncItem,
//                            isClipList
//                    )
//                }
//            }
//            is PicturePostHolder -> {
//                item?.also {
//                    holder.pictureRecycler.tag = position
//                    holder.onBind(
//                            item,
//                            null,
//                            position,
//                            adultListener,
//                            mTag,
//                            memberPostFuncItem
//                    )
//                }
//            }
//            is TextPostHolder -> {
//                item?.also {
//                    holder.onBind(
//                            it,
//                            null,
//                            position,
//                            adultListener,
//                            mTag,
//                            memberPostFuncItem
//                    )
//                }
//            }
        }
    }


    fun updateInternalItem(holder: RecyclerView.ViewHolder) {
        when (holder) {
            is PicturePostHolder -> {
                holder.pictureRecycler.adapter?.notifyDataSetChanged()
            }
        }
    }

    fun setupTag(tag: String) {
        mTag = tag
    }

}