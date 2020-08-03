package com.dabenxiang.mimi.view.adapter.viewHolder

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.TextContentItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_my_post_text_post.view.*
import kotlinx.android.synthetic.main.item_my_post_text_post.view.chip_group_tag
import kotlinx.android.synthetic.main.item_my_post_text_post.view.img_avatar
import kotlinx.android.synthetic.main.item_my_post_text_post.view.iv_comment
import kotlinx.android.synthetic.main.item_my_post_text_post.view.iv_like
import kotlinx.android.synthetic.main.item_my_post_text_post.view.iv_more
import kotlinx.android.synthetic.main.item_my_post_text_post.view.tv_comment_count
import kotlinx.android.synthetic.main.item_my_post_text_post.view.tv_follow
import kotlinx.android.synthetic.main.item_my_post_text_post.view.tv_like_count
import kotlinx.android.synthetic.main.item_my_post_text_post.view.tv_name
import kotlinx.android.synthetic.main.item_my_post_text_post.view.tv_time
import kotlinx.android.synthetic.main.item_my_post_text_post.view.tv_title
import timber.log.Timber
import java.util.*

class MyPostTextPostHolder(
    itemView: View,
    private val isMe: Boolean,
    private val isAdultTheme: Boolean
) : BaseViewHolder(itemView) {

    private val textPostItemLayout: ConstraintLayout = itemView.layout_text_post_item
    private val imgAvatar: ImageView = itemView.img_avatar
    private val tvName: TextView = itemView.tv_name
    private val tvTime: TextView = itemView.tv_time
    private val tvTitle: TextView = itemView.tv_title
    private val tvTextDesc: TextView = itemView.tv_text_desc
    private val tagChipGroup: ChipGroup = itemView.chip_group_tag
    private val ivLike: ImageView = itemView.iv_like
    private val tvLikeCount: TextView = itemView.tv_like_count
    private val ivComment: ImageView = itemView.iv_comment
    private val tvCommentCount: TextView = itemView.tv_comment_count
    private val ivMore: ImageView = itemView.iv_more
    private val tvFollow: TextView = itemView.tv_follow

    fun onBind(
        item: MemberPostItem,
        position: Int,
        myPostListener: MyPostFragment.MyPostListener,
        attachmentListener: AttachmentListener
    ) {

        textPostItemLayout.setBackgroundColor(App.self.getColor(if (isAdultTheme) R.color.color_black_4 else R.color.color_white_1))
        tvName.setTextColor(App.self.getColor(if (isAdultTheme) R.color.color_white_1 else R.color.color_black_1))
        tvTime.setTextColor(App.self.getColor(if (isAdultTheme) R.color.color_white_1_50 else R.color.color_black_1_50))
        tvTitle.setTextColor(App.self.getColor(if (isAdultTheme) R.color.color_white_1 else R.color.color_black_1))
        tvTextDesc.setTextColor(App.self.getColor(if (isAdultTheme) R.color.color_white_1 else R.color.color_black_1))
        tvLikeCount.setTextColor(App.self.getColor(if (isAdultTheme) R.color.color_white_1 else R.color.color_black_1))
        tvCommentCount.setTextColor(App.self.getColor(if (isAdultTheme) R.color.color_white_1 else R.color.color_black_1))
        ivComment.setImageResource(if (isAdultTheme) R.drawable.ico_messege_adult else R.drawable.ico_messege_adult_gray)
        ivMore.setImageResource(if (isAdultTheme) R.drawable.btn_more_white_n else R.drawable.btn_more_gray_n)

        tvName.text = item.postFriendlyName
        tvTime.text = GeneralUtils.getTimeDiff(item.creationDate, Date())
        tvTitle.text = item.title

        if (isMe) {
            tvFollow.visibility = View.GONE
        } else {
            tvFollow.visibility = View.VISIBLE
            tvFollow.setOnClickListener {
                myPostListener.onFollowClick(item, position, !item.isFollow)
            }
        }

        // FIXME: item.content json 資料格式有問題
        try {
            val contentItem = Gson().fromJson(item.content, TextContentItem::class.java)
            tvTextDesc.text = contentItem.text
        } catch (e: Exception) {
            Timber.e(e)
        }

        updateLikeAndFollowItem(item, position, myPostListener)

        if (LruCacheUtils.getLruCache(item.avatarAttachmentId.toString()) == null) {
            attachmentListener.onGetAttachment(
                item.avatarAttachmentId.toString(),
                position,
                AttachmentType.ADULT_TAB_TEXT
            )
        } else {
            val bitmap = LruCacheUtils.getLruCache(item.avatarAttachmentId.toString())
            Glide.with(imgAvatar.context)
                .load(bitmap)
                .circleCrop()
                .into(imgAvatar)
        }

        tagChipGroup.removeAllViews()
        item.tags?.forEach {
            val chip = LayoutInflater.from(tagChipGroup.context)
                .inflate(R.layout.chip_item, tagChipGroup, false) as Chip
            chip.text = it
            chip.setTextColor(tagChipGroup.context.getColor(if (isAdultTheme) R.color.color_white_1_50 else R.color.color_black_1_50))
            chip.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(
                    tagChipGroup.context,
                    if (isAdultTheme) R.color.color_black_6 else R.color.color_black_1_05
                )
            )
            chip.setOnClickListener { view ->
                myPostListener.onChipClick(PostType.VIDEO, (view as Chip).text.toString())
            }
            tagChipGroup.addView(chip)
        }

        ivComment.setOnClickListener {
            myPostListener.onCommentClick(item, AdultTabType.TEXT)
        }

        if (isMe) {
            ivMore.visibility = View.VISIBLE
            ivMore.setOnClickListener {
                myPostListener.onMoreClick(item)
            }
        } else {
            ivMore.visibility = View.GONE
        }

        textPostItemLayout.setOnClickListener {
            myPostListener.onItemClick(item, AdultTabType.TEXT)
        }
    }

    private fun updateLikeAndFollowItem(
        item: MemberPostItem,
        position: Int,
        myPostListener: MyPostFragment.MyPostListener
    ) {
        tvLikeCount.text = item.likeCount.toString()
        tvCommentCount.text = item.commentCount.toString()

        val likeType = item.likeType
        val isLike: Boolean
        if (likeType == LikeType.LIKE) {
            isLike = true
            ivLike.setImageResource(R.drawable.ico_nice_s)
        } else {
            isLike = false
            ivLike.setImageResource(if (isAdultTheme) R.drawable.ico_nice else R.drawable.ico_nice_gray)
        }

        ivLike.setOnClickListener {
            myPostListener.onLikeClick(item, position, !isLike)
        }

        updateFollow(item.isFollow)
    }

    private fun updateFollow(isFollow: Boolean) {
        tvFollow.setText(if (isFollow) R.string.followed else R.string.follow)
        tvFollow.setBackgroundResource(if (isFollow) R.drawable.bg_white_1_stroke_radius_16 else R.drawable.bg_red_1_stroke_radius_16)
        tvFollow.setTextColor(App.self.getColor(if (isFollow) R.color.color_white_1 else R.color.color_red_1))
    }

}