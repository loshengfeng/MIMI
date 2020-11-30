package com.dabenxiang.mimi.view.adapter.viewHolder

import android.content.res.ColorStateList
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.model.api.vo.MediaContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PostFavoriteItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.adapter.ClubFollowAdapter
import com.dabenxiang.mimi.view.adapter.ClubLikeAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import kotlinx.android.synthetic.main.head_video_info.view.*
import kotlinx.android.synthetic.main.item_clip_post.view.*
import kotlinx.android.synthetic.main.item_clip_post.view.tv_title
import kotlinx.android.synthetic.main.item_follow_club.view.*
import kotlinx.android.synthetic.main.item_follow_club.view.iv_photo
import kotlinx.android.synthetic.main.item_follow_club.view.tv_follow
import kotlinx.android.synthetic.main.item_follow_club.view.tv_name
import java.text.SimpleDateFormat
import java.util.*

class ClubLikeViewHolder(
    itemView: View,
    val listener: ClubLikeAdapter.EventListener
) : BaseAnyViewHolder<PostFavoriteItem>(itemView) {
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
    private val layoutClip: ConstraintLayout = itemView.layout_clip
    private val vSeparator: View = itemView.v_separator


    init {
//        itemView.setOnClickListener { data?.let { data -> listener.onDetail(data) } }
    }

    override fun updated(position: Int) {
//        data?.avatarAttachmentId?.let { id -> listener.onGetAttachment(id, ivPhoto) }
        tvName.text = data?.posterName
        tvTitle.text = data?.title
        tvTime.text = data?.postDate.let { date ->
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm",
                Locale.getDefault()
            ).format(date)
        }
        val contentItem = Gson().fromJson(data?.content.toString(), MediaContentItem::class.java)
        tvLength.text = contentItem?.shortVideo?.length
        contentItem.images?.also {images->
            if (!TextUtils.isEmpty(images[0].url)) {
                Glide.with(ivPhoto.context)
                    .load(images[0].url).placeholder(R.drawable.img_nopic_03).into(ivPhoto)
            } else {
//                listener.onGetAttachment(
//                    images[0].id.toLongOrNull(),
//                    ivPhoto,
//                    LoadImageType.PICTURE_THUMBNAIL
//                )
            }
        }

        tvFollow.visibility = View.VISIBLE
        when (data?.isFollow) {
            true -> {
                tvFollow.setTextColor(tvFollow.context.getColor(R.color.color_black_1_60))
                tvFollow.setBackgroundResource(R.drawable.bg_gray_6_radius_16)
                tvFollow.setText(R.string.followed)
            }
            else -> {
                tvFollow.setTextColor(tvFollow.context.getColor(R.color.color_red_1))
                tvFollow.setBackgroundResource(R.drawable.bg_red_1_stroke_radius_16)
                tvFollow.setText(R.string.follow)
            }
        }
//        setupChipGroup(data?.tags, data?.type)

        tvLikeCount.text = data?.likeCount.toString()
//        val res = if (data?.likeType == LikeType.LIKE) {
//            R.drawable.ico_nice_s
//        } else {
//            R.drawable.ico_nice_gray
//        }
//        tvLike.setCompoundDrawablesRelativeWithIntrinsicBounds(res, 0, 0, 0)

        tvFavoriteCount.text = data?.favoriteCount.toString()
        tvCommentCount.text = data?.commentCount.toString()
    }

    private fun setupChipGroup(list: List<String>?, type: Int?) {
        tagChipGroup.removeAllViews()

        if (list == null) {
            return
        }

        list.indices.mapNotNull {
            list[it]
        }.forEach {
            val chip = LayoutInflater.from(tagChipGroup.context)
                .inflate(R.layout.chip_item, tagChipGroup, false) as Chip
            chip.text = it
            chip.setTextColor(chip.context.getColor(R.color.color_black_1_50))
            chip.chipBackgroundColor =
                ColorStateList.valueOf(chip.context.getColor(R.color.color_black_1_10))
            chip.isClickable = true
            chip.setOnClickListener {
//                listener.onChipClick((it as Chip).text.toString(), type)
            }
            tagChipGroup.addView(chip)
        }
    }

    override fun updated() {}
}