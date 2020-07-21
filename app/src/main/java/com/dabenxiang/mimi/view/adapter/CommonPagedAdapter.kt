package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.callback.OnItemClickListener
import com.dabenxiang.mimi.model.api.vo.ContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.view.adapter.viewHolder.ClipPostHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.PicturePostHolder
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils.getLruCache
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import timber.log.Timber
import java.util.*

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
            else -> {
                //TODO:
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
                    when(it[0] as Int) {
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