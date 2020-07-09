package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.picturepost.PicturePostHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils.getLruCache
import com.google.android.material.chip.Chip
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
    }

    private var adultTabType: AdultTabType = AdultTabType.FOLLOW

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (adultTabType) {
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

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (holder) {
            is PicturePostHolder -> setupPicturePost(holder, position)
        }
    }

    fun setupAdultTabType(type: AdultTabType) {
        adultTabType = type
    }

    private fun setupPicturePost(holder: PicturePostHolder, position: Int) {
        val item = getItem(position)

        holder.name.text = item?.postFriendlyName
        holder.likeCount.text = item?.likeCount.toString()
        holder.commentCount.text = item?.commentCount.toString()
        holder.time.text = GeneralUtils.getTimeDiff(item?.creationDate ?: Date(), Date())
        holder.title.text = item?.title

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

        if (getLruCache(item?.avatarAttachmentId!!) == null) {
            attachmentListener.onGetAttachment(item.avatarAttachmentId, position)
        } else {
            val bitmap = getLruCache(item.avatarAttachmentId)
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

//        holder.pictureViewPager.adapter = PictureAdapter()


        holder.likeImage.setOnClickListener {
            adultListener.doLike()
        }

        holder.commentImage.setOnClickListener {
            adultListener.comment()
        }

        holder.moreImage.setOnClickListener {
            adultListener.more()
        }
    }

}