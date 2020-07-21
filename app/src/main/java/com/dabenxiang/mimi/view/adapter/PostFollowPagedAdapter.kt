package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.model.api.vo.PostFollowItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.adapter.viewHolder.ClipPostHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.PicturePostHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.TextPostHolder
import com.dabenxiang.mimi.view.base.BaseViewHolder

class PostFollowPagedAdapter(
    val context: Context,
    private val adultListener: AdultListener,
    private val attachmentListener: AttachmentListener
) : PagedListAdapter<PostFollowItem, BaseViewHolder>(diffCallback) {

    companion object {

        const val PAYLOAD_UPDATE_LIKE_AND_FOLLOW_UI = 0
        const val VIEW_TYPE_CLIP = 0
        const val VIEW_TYPE_PICTURE = 1
        const val VIEW_TYPE_TEXT = 2

        val diffCallback = object : DiffUtil.ItemCallback<PostFollowItem>() {
            override fun areItemsTheSame(
                oldItem: PostFollowItem,
                newItem: PostFollowItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: PostFollowItem,
                newItem: PostFollowItem
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

    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {

    }

//    fun setupAdultTabType(type: AdultTabType) {
//        adultTabType = type
//    }

}