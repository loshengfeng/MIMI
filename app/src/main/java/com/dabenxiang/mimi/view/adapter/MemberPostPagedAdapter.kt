package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.adapter.viewHolder.ClipPostHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.PicturePostHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.TextPostHolder
import com.dabenxiang.mimi.view.base.BaseViewHolder

class MemberPostPagedAdapter(
    val context: Context,
    private val adultListener: AdultListener,
    private var mTag: String = "",
    private val memberPostFuncItem: MemberPostFuncItem = MemberPostFuncItem()
) : PagedListAdapter<MemberPostItem, BaseViewHolder>(diffCallback) {

    companion object {
        const val PAYLOAD_UPDATE_LIKE_AND_FOLLOW_UI = 0
        const val VIEW_TYPE_CLIP = 0
        const val VIEW_TYPE_PICTURE = 1
        const val VIEW_TYPE_TEXT = 2

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

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when (item?.type) {
            PostType.VIDEO -> VIEW_TYPE_CLIP
            PostType.IMAGE -> VIEW_TYPE_PICTURE
            else -> VIEW_TYPE_TEXT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
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
            else -> {
                TextPostHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_text_post, parent, false)
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
        when (holder) {
            is ClipPostHolder -> {
                item?.also {
                    holder.onBind(
                        it,
                        currentList,
                        position,
                        adultListener,
                        mTag,
                        memberPostFuncItem
                    )
                }
            }
            is PicturePostHolder -> {
                payloads.takeIf { it.isNotEmpty() }?.also {
                    when (it[0] as Int) {
                        PAYLOAD_UPDATE_LIKE_AND_FOLLOW_UI -> {
                            item?.also { item ->
                                holder.updateLikeAndFollowItem(item, position, adultListener)
                            }
                        }
                    }
                } ?: run {
                    item?.also {
                        holder.pictureRecycler.tag = position
                        holder.onBind(it, position, adultListener, mTag, memberPostFuncItem)
                    }
                }
            }
            is TextPostHolder -> {
                item?.also {
                    holder.onBind(
                        it,
                        position,
                        adultListener,
                        mTag,
                        memberPostFuncItem
                    )
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

    fun setupTag(tag: String) {
        mTag = tag
    }
}