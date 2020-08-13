package com.dabenxiang.mimi.view.adapter.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ClubFollowItem
import com.dabenxiang.mimi.view.adapter.ClubFollowAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.android.synthetic.main.item_follow_club.view.*

class ClubFollowViewHolder(
    itemView: View,
    val listener: ClubFollowAdapter.EventListener
) : BaseAnyViewHolder<ClubFollowItem>(itemView) {
    private val ivPhoto: ImageView = itemView.iv_photo
    private val tvName: TextView = itemView.tv_name
    private val tvSubTitle: TextView = itemView.tv_sub_title
    private val tvFollow: TextView = itemView.tv_follow
    private val tvClubFollow:TextView = itemView.tv_club_follow
    private val tvClubPost:TextView = itemView.tv_club_post

    init {
        itemView.setOnClickListener { data?.let { data -> listener.onDetail(data) } }
        tvFollow.setOnClickListener { data?.clubId?.let { clubId -> listener.onCancelFollow(clubId) }}
    }

    override fun updated(position: Int) {
        val avatarId = data?.avatarAttachmentId.toString()
        if (avatarId != LruCacheUtils.ZERO_ID) {
            val bitmap = LruCacheUtils.getLruCache(avatarId)
            if (bitmap == null) {
                listener.onGetAttachment(data!!.avatarAttachmentId.toString(), position)
            } else {
                Glide.with(ivPhoto.context).load(bitmap).circleCrop().into(ivPhoto)
            }
        } else {
            Glide.with(ivPhoto.context).load(R.drawable.default_profile_picture).circleCrop().into(ivPhoto)
        }

        tvName.text = data?.name ?: ""
        tvSubTitle.text = data?.description
        tvClubFollow.text = data?.followerCount.toString()
        tvClubPost.text = data?.postCount.toString()
    }

    override fun updated() {}
}