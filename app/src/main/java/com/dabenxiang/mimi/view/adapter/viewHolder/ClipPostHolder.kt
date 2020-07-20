package com.dabenxiang.mimi.view.adapter.viewHolder

import android.content.res.ColorStateList
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.model.api.vo.ContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_clip_post.view.*
import timber.log.Timber
import java.util.*

class ClipPostHolder(itemView: View) : BaseViewHolder(itemView) {
    val clClipPost: ConstraintLayout = itemView.cl_clip_post
    val ivAvatar: ImageView = itemView.img_avatar
    val name: TextView = itemView.tv_name
    val time: TextView = itemView.tv_time
    val follow: TextView = itemView.tv_follow
    val title: TextView = itemView.tv_title
    val ivPhoto: ImageView = itemView.iv_photo
    val tvLength: TextView = itemView.tv_length
    val tagChipGroup: ChipGroup = itemView.chip_group_tag
    val likeImage: ImageView = itemView.iv_like
    val likeCount: TextView = itemView.tv_like_count
    val commentImage: ImageView = itemView.iv_comment
    val commentCount: TextView = itemView.tv_comment_count
    val moreImage: ImageView = itemView.iv_more

    fun onBind(itemList: List<MemberPostItem>, position: Int, adultListener: AdultListener, attachmentListener: AttachmentListener) {
        Timber.d("@@onBind")
        val item = itemList[position]
        name.text = item.postFriendlyName
        time.text = GeneralUtils.getTimeDiff(item.creationDate, Date())
        title.text = item.title
        updateLikeAndFollowItem(item, position, adultListener)

        if (LruCacheUtils.getLruCache(item.avatarAttachmentId.toString()) == null) {
            attachmentListener.onGetAttachment(
                item.avatarAttachmentId.toString(),
                position,
                AttachmentType.ADULT_TAB_CLIP
            )
        } else {
            val bitmap = LruCacheUtils.getLruCache(item.avatarAttachmentId.toString())
            Glide.with(ivAvatar.context)
                .load(bitmap)
                .circleCrop()
                .into(ivAvatar)
        }

        tagChipGroup.removeAllViews()
        item.tags?.forEach {
            val chip = LayoutInflater.from(tagChipGroup.context)
                .inflate(R.layout.chip_item, tagChipGroup, false) as Chip
            chip.text = it
            chip.setTextColor(tagChipGroup.context.getColor(R.color.color_white_1_50))
            chip.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(tagChipGroup.context, R.color.adult_color_status_bar)
            )
            tagChipGroup.addView(chip)
        }

        val contentItem = Gson().fromJson(item.content, ContentItem::class.java)

        tvLength.text = contentItem.shortVideo?.length ?: "00:00"
        contentItem.images?.takeIf { it.isNotEmpty() }?.also { images ->
            images[0].also { image ->
                if (TextUtils.isEmpty(image.url)) {
                    image.id.takeIf { !TextUtils.isEmpty(it) }?.also { id ->
                        LruCacheUtils.getLruCache(id)?.also { bitmap ->
                            Glide.with(ivPhoto.context).load(bitmap).into(ivPhoto)
                        } ?: run {
                            attachmentListener.onGetAttachment(
                                id,
                                position,
                                AttachmentType.ADULT_TAB_CLIP
                            )
                        }
                    }
                } else {
                    Glide.with(ivPhoto.context).load(image.url).into(ivPhoto)
                }
            }
        }

        commentImage.setOnClickListener {
            adultListener.onClipCommentClick(itemList, position)
        }

        moreImage.setOnClickListener {
            adultListener.onMoreClick(item)
        }

        clClipPost.setOnClickListener {
            adultListener.onClipItemClick(itemList, position)
        }
    }

    fun updateLikeAndFollowItem(item: MemberPostItem, position: Int, adultListener: AdultListener) {
        Timber.d("@@updateLikeAndFollowItem")
        likeCount.text = item.likeCount.toString()
        commentCount.text = item.commentCount.toString()

        val isFollow = item.isFollow ?: false
        if (isFollow) {
            follow.text = follow.context.getString(R.string.followed)
            follow.background = follow.context.getDrawable(R.drawable.bg_white_1_stroke_radius_16)
            follow.setTextColor(follow.context.getColor(R.color.color_white_1))
        } else {
            follow.text = follow.context.getString(R.string.follow)
            follow.background = follow.context.getDrawable(R.drawable.bg_red_1_stroke_radius_16)
            follow.setTextColor(follow.context.getColor(R.color.color_red_1))
        }

        val likeType = item.likeType
        val isLike: Boolean
        if (likeType == LikeType.LIKE) {
            isLike = true
            likeImage.setImageResource(R.drawable.ico_nice_s)
        } else {
            isLike = false
            likeImage.setImageResource(R.drawable.ico_nice)
        }

        follow.setOnClickListener {
            adultListener.onFollowPostClick(item, position, !isFollow)
        }

        likeImage.setOnClickListener {
            adultListener.onLikeClick(item, position, !isLike)
        }
    }
}