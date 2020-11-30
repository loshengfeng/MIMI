package com.dabenxiang.mimi.view.adapter.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.adapter.ClubFollowAdapter
import com.dabenxiang.mimi.view.adapter.ClubLikeAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.item_clip_post.view.*
import kotlinx.android.synthetic.main.item_follow_club.view.*
import kotlinx.android.synthetic.main.item_follow_club.view.iv_photo
import kotlinx.android.synthetic.main.item_follow_club.view.tv_follow
import kotlinx.android.synthetic.main.item_follow_club.view.tv_name

class ClubLikeViewHolder(
    itemView: View,
    val listener: ClubLikeAdapter.EventListener
) : BaseAnyViewHolder<MemberPostItem>(itemView) {
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
        itemView.setOnClickListener { data?.let { data -> listener.onDetail(data) } }
    }

    override fun updated(position: Int) {
//        data?.avatarAttachmentId?.let { id -> listener.onGetAttachment(id, ivPhoto) }
//        tvName.text = data?.name ?: ""
//        tvSubTitle.text = data?.description
//        tvClubFollow.text = data?.followerCount.toString()
//        tvClubPost.text = data?.postCount.toString()
//        clFollow.setOnClickListener {
//            data?.clubId?.let { clubId ->
//                listener.onCancelFollow(
//                    clubId,
//                    position
//                )
//            }
//        }
    }

    override fun updated() {}
}