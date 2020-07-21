package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.view.adapter.viewHolder.ClipPostHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.PicturePostHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.TextPostHolder
import com.dabenxiang.mimi.view.base.BaseViewHolder

class CommonPagedAdapter(
    val context: Context,
    private val adultListener: AdultListener,
    private val attachmentListener: AttachmentListener
) : PagedListAdapter<MemberPostItem, BaseViewHolder>(diffCallback) {

    companion object {
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
        const val PAYLOAD_UPDATE_LIKE_AND_FOLLOW_UI = 0
    }

    private var adultTabType: AdultTabType = AdultTabType.FOLLOW

    val viewHolderMap = hashMapOf<Int, BaseViewHolder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (adultTabType) {
            AdultTabType.CLIP -> {
                ClipPostHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_clip_post, parent, false)
                )
            }
            AdultTabType.PICTURE -> {
                PicturePostHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_picture_post, parent, false)
                )
            }
            AdultTabType.TEXT -> {
                TextPostHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_text_post, parent, false)
                )
            }
            else -> {
                //TODO: 關注
                PicturePostHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_picture_post, parent, false)
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
        when(holder) {
            is ClipPostHolder -> {
                item?.also { holder.onBind(it, currentList, position, adultListener, attachmentListener) }
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
                        holder.onBind(it, position, adultListener, attachmentListener)
                    }
                }
            }
            is TextPostHolder -> {
                payloads.takeIf { it.isNotEmpty() }?.also {
                    when (it[0] as Int) {
                        PAYLOAD_UPDATE_LIKE_AND_FOLLOW_UI -> {
                            currentList?.also { itemList ->
                                holder.updateLikeAndFollowItem(
                                    itemList.toList(),
                                    position,
                                    adultListener
                                )
                            }
                        }
                    }
                }?: run {
                    currentList?.also {
                        holder.onBind(it.toList(), position, adultListener, attachmentListener)
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
    }

    fun setupAdultTabType(type: AdultTabType) {
        adultTabType = type
    }

    fun updateInternalItem(holder: BaseViewHolder) {
        when (holder) {
            is PicturePostHolder -> {
                holder.pictureRecycler.adapter?.notifyDataSetChanged()
            }
        }
    }
}