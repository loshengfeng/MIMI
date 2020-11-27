package com.dabenxiang.mimi.view.adapter.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.view.adapter.ClubFollowAdapter
import com.dabenxiang.mimi.view.adapter.ClubLikeAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import kotlinx.android.synthetic.main.item_follow_club.view.*

class ClubLikeViewHolder(
    itemView: View,
    val listener: ClubLikeAdapter.EventListener
) : BaseAnyViewHolder<ClubFollowItem>(itemView) {
    private val ivPhoto: ImageView = itemView.iv_photo
    private val tvName: TextView = itemView.tv_name
    private val tvSubTitle: TextView = itemView.tv_sub_title
    private val tvClubFollow: TextView = itemView.tv_club_follow
    private val tvClubPost: TextView = itemView.tv_club_post
    private val clFollow: ConstraintLayout = itemView.cl_follow

    init {
        itemView.setOnClickListener { data?.let { data -> listener.onDetail(data) } }
    }

    override fun updated(position: Int) {
        data?.avatarAttachmentId?.let { id -> listener.onGetAttachment(id, ivPhoto) }
        tvName.text = data?.name ?: ""
        tvSubTitle.text = data?.description
        tvClubFollow.text = data?.followerCount.toString()
        tvClubPost.text = data?.postCount.toString()
        clFollow.setOnClickListener {
            data?.clubId?.let { clubId ->
                listener.onCancelFollow(
                    clubId,
                    position
                )
            }
        }
    }

    override fun updated() {}
}