package com.dabenxiang.mimi.view.adapter.viewHolder

import android.content.res.ColorStateList
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
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.TextContentItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_text_post.view.*
import java.lang.Exception
import java.util.*

class TextPostHolder(itemView: View) : BaseViewHolder(itemView) {

    val textPostItemLayout: ConstraintLayout = itemView.layout_text_post_item
    val avatarImg: ImageView = itemView.img_avatar
    val name: TextView = itemView.tv_name
    val time: TextView = itemView.tv_time
    val follow: TextView = itemView.tv_follow
    val title: TextView = itemView.tv_title
    val desc: TextView = itemView.tv_text_desc
    val tagChipGroup: ChipGroup = itemView.chip_group_tag
    val likeImage: ImageView = itemView.iv_like
    val likeCount: TextView = itemView.tv_like_count
    val commentImage: ImageView = itemView.iv_comment
    val commentCount: TextView = itemView.tv_comment_count
    val moreImage: ImageView = itemView.iv_more

    fun onBind(
        itemList: List<MemberPostItem>,
        position: Int,
        adultListener: AdultListener,
        attachmentListener: AttachmentListener
    ) {
        val item = itemList[position]

        name.text = item.postFriendlyName
        time.text = GeneralUtils.getTimeDiff(item.creationDate, Date())
        title.text = item.title

        // FIXME: item.content json 資料格式有問題
        try {
            val contentItem = Gson().fromJson(item.content, TextContentItem::class.java)
            desc.text = contentItem.text
        } catch (e: Exception) {
//            Timber.e(e)
        }

        updateLikeAndFollowItem(itemList, position, adultListener)

        if (LruCacheUtils.getLruCache(item.avatarAttachmentId.toString()) == null) {
            attachmentListener.onGetAttachment(
                item.avatarAttachmentId.toString(),
                position,
                AttachmentType.ADULT_TAB_TEXT
            )
        } else {
            val bitmap = LruCacheUtils.getLruCache(item.avatarAttachmentId.toString())
            Glide.with(avatarImg.context)
                .load(bitmap)
                .circleCrop()
                .into(avatarImg)
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

        commentImage.setOnClickListener {
            item.also { adultListener.onCommentClick(it, AdultTabType.TEXT) }
        }

        moreImage.setOnClickListener {
            adultListener.onMoreClick(item)
        }

        textPostItemLayout.setOnClickListener {
            item.also { adultListener.onItemClick(item, AdultTabType.TEXT) }
        }
    }

    fun updateLikeAndFollowItem(
        itemList: List<MemberPostItem>,
        position: Int,
        adultListener: AdultListener
    ) {
        val item = itemList[position]

        likeCount.text = item.likeCount.toString()
        commentCount.text = item.commentCount.toString()

        val isFollow = item.isFollow
        if (isFollow) {
            follow.text = follow.context.getString(R.string.followed)
            follow.background =
                follow.context.getDrawable(R.drawable.bg_white_1_stroke_radius_16)
            follow.setTextColor(follow.context.getColor(R.color.color_white_1))
        } else {
            follow.text = follow.context.getString(R.string.follow)
            follow.background =
                follow.context.getDrawable(R.drawable.bg_red_1_stroke_radius_16)
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