package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.adapter.viewHolder.*
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import timber.log.Timber

class MemberPostPagedAdapter(
    val context: Context,
    private val adultListener: AdultListener,
    private var mTag: String?= null,
    private val memberPostFuncItem: MemberPostFuncItem = MemberPostFuncItem(),
    private val isClipList: Boolean = false
) : PagedListAdapter<MemberPostItem, BaseViewHolder>(diffCallback) {

    companion object {
        const val PAYLOAD_UPDATE_LIKE = 0
        const val PAYLOAD_UPDATE_FOLLOW = 1
        const val PAYLOAD_UPDATE_FAVORITE= 2
        const val VIEW_TYPE_CLIP = 0
        const val VIEW_TYPE_PICTURE = 1
        const val VIEW_TYPE_TEXT = 2
        const val VIEW_TYPE_AD = 3
        const val VIEW_TYPE_DELETED = 4

        val diffCallback = object : DiffUtil.ItemCallback<MemberPostItem>() {
            override fun areItemsTheSame(
                oldItem: MemberPostItem,
                newItem: MemberPostItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: MemberPostItem,
                newItem: MemberPostItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    val viewHolderMap = hashMapOf<Int, BaseViewHolder>()

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

    override fun onBindViewHolder(
        holder: BaseViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        viewHolderMap[position] = holder
        val item = getItem(position)

        Timber.i("MemberPostPagedAdapter item=$item")
        when (holder) {
            is AdHolder -> {
                Glide.with(context).load(item?.adItem?.href).into(holder.adImg)
                holder.adImg.setOnClickListener {
                    GeneralUtils.openWebView(context, item?.adItem?.target ?: "")
                }
            }
            is ClipPostHolder -> {
                item?.also {
//                    payloads.takeIf { it.isNotEmpty() }?.also {
//                        when (it[0] as Int) {
//                            PAYLOAD_UPDATE_FOLLOW -> holder.updateFollow(item.isFollow)
//                        }
//                    } ?: run {
                        holder.onBind(
                            item,
                            currentList,
                            position,
                            adultListener,
                            mTag,
                            memberPostFuncItem,
                            isClipList
                        )
//                    }
                }
            }
            is PicturePostHolder -> {
                item?.also {
//                    payloads.takeIf { it.isNotEmpty() }?.also {
//                        when (it[0] as Int) {
//                            PAYLOAD_UPDATE_FOLLOW -> holder.updateFollow(item.isFollow)
//                            PAYLOAD_UPDATE_LIKE -> holder.updateLikeAndFollowItem(
//                                item,
//                                currentList,
//                                memberPostFuncItem
//                            )
//                        }
//                    } ?: run {
                        holder.pictureRecycler.tag = position
                        holder.onBind(
                            item,
                            currentList,
                            position,
                            adultListener,
                            mTag,
                            memberPostFuncItem
                        )
//                    }
                }
            }
            is TextPostHolder -> {
                item?.also {
//                    payloads.takeIf { it.isNotEmpty() }?.also {
//                        when (it[0] as Int) {
//                            PAYLOAD_UPDATE_FOLLOW -> holder.updateFollow(item.isFollow)
//                        }
//                    } ?: run {
                        holder.onBind(
                            it,
                            currentList,
                            position,
                            adultListener,
                            mTag,
                            memberPostFuncItem
                        )
//                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
    }

    fun updateInternalItem(holder: BaseViewHolder) {
        when (holder) {
            is PicturePostHolder -> {
                holder.pictureRecycler.adapter?.notifyDataSetChanged()
            }
        }
    }

    fun setupTag(tag: String?) {
        mTag = tag
    }
}