package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.adapter.viewHolder.MyPostClipPostHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.MyPostPicturePostHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.MyPostTextPostHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.PicturePostHolder
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.mypost.MyPostFragment

class MyPostPagedAdapter(
    val context: Context,
    private val isMe: Boolean,
    private val isAdultTheme: Boolean,
    private val myPostListener: MyPostFragment.MyPostListener,
    private val attachmentListener: AttachmentListener,
    private val memberPostFuncItem: MemberPostFuncItem
) : PagedListAdapter<MemberPostItem, BaseViewHolder>(diffCallback) {

    companion object {
        const val PAYLOAD_UPDATE_LIKE = 0
        const val PAYLOAD_UPDATE_FAVORITE = 1
        const val PAYLOAD_UPDATE_FOLLOW = 2
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
                MyPostClipPostHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_clip_post, parent, false), isAdultTheme
                )
            }
            VIEW_TYPE_PICTURE -> {
                MyPostPicturePostHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_picture_post, parent, false), isAdultTheme
                )
            }
            else -> {
                MyPostTextPostHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_text_post, parent, false), isAdultTheme
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

        item?.also {
            when (holder) {
                is MyPostClipPostHolder -> {
                    payloads.takeIf { it.isNotEmpty() }?.also {
                        when (it[0] as Int) {
                            PAYLOAD_UPDATE_LIKE -> holder.updateLike(item)
                            PAYLOAD_UPDATE_FAVORITE -> holder.updateFavorite(item)
                            PAYLOAD_UPDATE_FOLLOW -> holder.updateFollow(item)
                        }
                    } ?: run {
                        holder.onBind(
                            it,
                            currentList,
                            position,
                            myPostListener,
                            attachmentListener
                        )
                    }
                }
                is MyPostPicturePostHolder -> {
                    payloads.takeIf { it.isNotEmpty() }?.also {
                        when (it[0] as Int) {
                            PAYLOAD_UPDATE_LIKE -> holder.updateLike(item)
                            PAYLOAD_UPDATE_FOLLOW -> holder.updateFollow(item)
                        }
                    } ?: run {
                        holder.pictureRecycler.tag = position
                        holder.onBind(it, currentList, position, myPostListener, attachmentListener, memberPostFuncItem)
                    }
                }
                is MyPostTextPostHolder -> {
                    payloads.takeIf { it.isNotEmpty() }?.also {
                        when (it[0] as Int) {
                            PAYLOAD_UPDATE_LIKE -> holder.updateLike(item)
                            PAYLOAD_UPDATE_FOLLOW -> holder.updateFollow(item)
                        }
                    } ?: run {
                        holder.onBind(it, currentList, position, myPostListener, attachmentListener)
                    }
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
}