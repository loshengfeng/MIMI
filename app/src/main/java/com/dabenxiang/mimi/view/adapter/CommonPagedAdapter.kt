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
        when(holder) {
            is ClipPostHolder -> {
                currentList?.also { holder.onBind(it.toList(), position, adultListener, attachmentListener) }
            }
            is PicturePostHolder -> {
                payloads.takeIf { it.isNotEmpty() }?.also {
                    when(it[0] as Int) {
                        PAYLOAD_UPDATE_LIKE_AND_FOLLOW_UI -> {
                            updateLikeAndFollowItem(holder, position)
                        }
                    }
                } ?: run {
                    holder.pictureRecycler.tag = position
                    viewHolderMap[position] = holder
                    setupPicturePost(holder, position)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
    }

    fun setupAdultTabType(type: AdultTabType) {
        adultTabType = type
    }

    private fun setupPicturePost(holder: PicturePostHolder, position: Int) {
        val item = getItem(position)

        holder.name.text = item?.postFriendlyName
        holder.time.text = GeneralUtils.getTimeDiff(item?.creationDate ?: Date(), Date())
        holder.title.text = item?.title
        updateLikeAndFollowItem(holder, position)

        if (getLruCache(item?.avatarAttachmentId.toString()) == null) {
            attachmentListener.onGetAttachment(
                item?.avatarAttachmentId.toString(),
                position,
                AttachmentType.ADULT_TAB_PICTURE
            )
        } else {
            val bitmap = getLruCache(item?.avatarAttachmentId.toString())
            Glide.with(context)
                .load(bitmap)
                .circleCrop()
                .into(holder.avatarImg)
        }

        holder.tagChipGroup.removeAllViews()
        item?.tags?.forEach {
            val chip = LayoutInflater.from(holder.tagChipGroup.context)
                .inflate(R.layout.chip_item, holder.tagChipGroup, false) as Chip
            chip.text = it
            chip.setTextColor(context.getColor(R.color.color_white_1_50))
            chip.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.adult_color_status_bar)
            )
            holder.tagChipGroup.addView(chip)
        }

        val contentItem = Gson().fromJson(item?.content, ContentItem::class.java)
        if (holder.pictureRecycler.adapter == null || holder.pictureCount.tag != position) {
            holder.pictureCount.tag = position
            holder.pictureRecycler.layoutManager = LinearLayoutManager(
                context, LinearLayoutManager.HORIZONTAL, false
            )

            holder.pictureRecycler.adapter = PictureAdapter(
                context,
                attachmentListener,
                contentItem.images,
                position,
                object : OnItemClickListener {
                    override fun onItemClick() {
                        adultListener.onItemClick(item!!)
                    }
                }
            )
            holder.pictureRecycler.onFlingListener = null
            LinearSnapHelper().attachToRecyclerView(holder.pictureRecycler)

            holder.pictureRecycler.setOnScrollChangeListener { _, _, _, _, _ ->
                val currentPosition =
                    (holder.pictureRecycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                holder.pictureCount.text =
                    "${currentPosition + 1}/${contentItem.images?.size}"
            }

            holder.pictureCount.text = "1/${contentItem.images?.size}"
        }

        holder.commentImage.setOnClickListener {
            adultListener.onCommentClick(item!!)
        }

        holder.moreImage.setOnClickListener {
            adultListener.onMoreClick(item!!)
        }

        holder.picturePostItemLayout.setOnClickListener {
            adultListener.onItemClick(item!!)
        }
    }

    fun updateInternalItem(holder: BaseViewHolder) {
        when (holder) {
            is PicturePostHolder -> {
                holder.pictureRecycler.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun updateLikeAndFollowItem(holder: PicturePostHolder, position: Int) {
        val item = getItem(position)

        holder.likeCount.text = item?.likeCount.toString()
        holder.commentCount.text = item?.commentCount.toString()

        val isFollow = item?.isFollow ?: false
        if (isFollow) {
            holder.follow.text = context.getString(R.string.followed)
            holder.follow.background =
                context.getDrawable(R.drawable.bg_white_1_stroke_radius_16)
            holder.follow.setTextColor(context.getColor(R.color.color_white_1))
        } else {
            holder.follow.text = context.getString(R.string.follow)
            holder.follow.background =
                context.getDrawable(R.drawable.bg_red_1_stroke_radius_16)
            holder.follow.setTextColor(context.getColor(R.color.color_red_1))
        }

        val likeType = item?.likeType ?: LikeType.DISLIKE
        val isLike: Boolean
        if (likeType == LikeType.LIKE) {
            isLike = true
            holder.likeImage.setImageResource(R.drawable.ico_nice_s)
        } else {
            isLike = false
            holder.likeImage.setImageResource(R.drawable.ico_nice)
        }

        holder.follow.setOnClickListener {
            adultListener.onFollowPostClick(item!!, position, !isFollow)
        }

        holder.likeImage.setOnClickListener {
            adultListener.onLikeClick(item!!, position, !isLike)
        }

    }
}