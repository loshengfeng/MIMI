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
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.model.api.vo.MediaContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
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
import kotlinx.android.synthetic.main.item_my_post_clip_post.view.*
import java.util.*

class MyPostClipPostHolder(
    itemView: View,
    private val isMe: Boolean,
    private val isAdultTheme: Boolean
) : BaseViewHolder(itemView) {

    private val clClipPost: ConstraintLayout = itemView.cl_clip_post
    private val ivAvatar: ImageView = itemView.img_avatar
    private val tvName: TextView = itemView.tv_name
    private val tvTime: TextView = itemView.tv_time
    private val tvTitle: TextView = itemView.tv_title
    private val tvFollow: TextView = itemView.tv_follow
    private val ivPhoto: ImageView = itemView.iv_photo
    private val tvLength: TextView = itemView.tv_length
    private val tagChipGroup: ChipGroup = itemView.chip_group_tag
    private val ivLike: ImageView = itemView.iv_like
    private val tvLikeCount: TextView = itemView.tv_like_count
    private val ivComment: ImageView = itemView.iv_comment
    private val tvCommentCount: TextView = itemView.tv_comment_count
    private val ivMore: ImageView = itemView.iv_more
    private val ivFavorite: ImageView = itemView.iv_favorite
    private val tvFavoriteCount: TextView = itemView.tv_favorite_count

    fun onBind(
        item: MemberPostItem,
        itemList: List<MemberPostItem>?,
        position: Int,
        myPostListener: MyPostFragment.MyPostListener,
        attachmentListener: AttachmentListener
    ) {
        clClipPost.setBackgroundColor(App.self.getColor(if (isAdultTheme) R.color.color_black_4 else R.color.color_white_1))
        tvName.setTextColor(App.self.getColor(if (isAdultTheme) R.color.color_white_1 else R.color.color_black_1))
        tvTime.setTextColor(App.self.getColor(if (isAdultTheme) R.color.color_white_1_50 else R.color.color_black_1_50))
        tvTitle.setTextColor(App.self.getColor(if (isAdultTheme) R.color.color_white_1 else R.color.color_black_1))
        tvLikeCount.setTextColor(App.self.getColor(if (isAdultTheme) R.color.color_white_1 else R.color.color_black_1))
        tvFavoriteCount.setTextColor(App.self.getColor(if (isAdultTheme) R.color.color_white_1 else R.color.color_black_1))
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
            updateFollow(item.isFollow)
            tvFollow.setOnClickListener {
                itemList?.also {
                    myPostListener.onFollowClick(item, position, !item.isFollow)
                }
            }
        }

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

        val contentItem = Gson().fromJson(item.content, MediaContentItem::class.java)

        tvLength.text = contentItem.shortVideo?.length
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

        ivComment.setOnClickListener {
            itemList?.also { myPostListener.onClipCommentClick(it, position) }
        }

        updateLikeAndFollowItem(item, position, myPostListener)

        if (isMe) {
            ivMore.visibility = View.VISIBLE
            ivMore.setOnClickListener {
                myPostListener.onMoreClick(item)
            }
        } else {
            ivMore.visibility = View.GONE
        }

        clClipPost.setOnClickListener {
            itemList?.also { myPostListener.onClipItemClick(it, position) }
        }

        ivFavorite.setOnClickListener {
            item.isFavorite = !item.isFavorite
            myPostListener.onFavoriteClick(
                item,
                position,
                item.isFavorite,
                AttachmentType.ADULT_HOME_CLIP
            )
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

        tvFavoriteCount.text = item.favoriteCount.toString()
        if (item.isFavorite) {
            ivFavorite.setImageResource(R.drawable.btn_favorite_white_s)
        } else {
            ivFavorite.setImageResource(if (isAdultTheme) R.drawable.btn_favorite_white_n else R.drawable.btn_favorite_n)
        }

        updateFollow(item.isFollow)
    }

    private fun updateFollow(isFollow: Boolean) {
        tvFollow.setText(if (isFollow) R.string.followed else R.string.follow)
        tvFollow.setBackgroundResource(if (isFollow) R.drawable.bg_white_1_stroke_radius_16 else R.drawable.bg_red_1_stroke_radius_16)
        tvFollow.setTextColor(App.self.getColor(if (isFollow) R.color.color_white_1 else R.color.color_red_1))
    }

}