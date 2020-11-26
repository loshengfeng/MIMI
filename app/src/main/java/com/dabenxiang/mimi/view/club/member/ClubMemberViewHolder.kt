package com.dabenxiang.mimi.view.club.member

import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.club.adapter.ClubFuncItem
import kotlinx.android.synthetic.main.item_club_member.view.*

/**
 * VAI4.1.6_圈子頁
 */
class ClubMemberViewHolder(view: View) : BaseViewHolder(view) {
    val clMemberPost = view.cl_club_member
    val ivAvatar = view.iv_avatar
    val tvTitle = view.tv_title
    val tvDesc = view.tv_desc
    val tvFollowCount = view.tv_follow_count
    val tvPostCount = view.tv_post_count
    val tvFollow = view.tv_follow
    val rvPost = view.rv_post

    fun onBind(item: MemberClubItem, clubFuncItem: ClubFuncItem, position: Int) {
        tvTitle.text = item.title
        tvDesc.text = item.description
        tvFollowCount.text = item.followerCount.toString()
        tvPostCount.text = item.postCount.toString()

        val isFollow = item.isFollow ?: false
        if (isFollow) {
            tvFollow.text = tvFollow.context.getString(R.string.followed)
            tvFollow.background =
                tvFollow.context.getDrawable(R.drawable.bg_white_1_stroke_radius_16)
            tvFollow.setTextColor(tvFollow.context.getColor(R.color.color_black_1_60))
        } else {
            tvFollow.text = tvFollow.context.getString(R.string.follow)
            tvFollow.background = tvFollow.context.getDrawable(R.drawable.bg_red_1_stroke_radius_16)
            tvFollow.setTextColor(tvFollow.context.getColor(R.color.color_red_1))
        }

        tvFollow.setOnClickListener {
            clubFuncItem.onFollowClick(
                item,
                !(item.isFollow ?: false)
            ) { isFollow -> updateFollowItem(isFollow) }
        }

        if (rvPost.adapter == null || rvPost.tag != position) {
            rvPost.tag = position
            rvPost.adapter = ClubMemberPostAdapter(rvPost.context, item.posts, item, clubFuncItem)
        }

        item.avatarAttachmentId.also {id->
            clubFuncItem.getBitmap(id, ivAvatar, LoadImageType.AVATAR)
        }

        clMemberPost.setOnClickListener {
            clubFuncItem.onItemClick(item)
        }
    }

    private fun updateFollowItem(isFollow: Boolean) {
        if (isFollow) {
            tvFollow.text = tvFollow.context.getString(R.string.followed)
            tvFollow.background =
                tvFollow.context.getDrawable(R.drawable.bg_white_1_stroke_radius_16)
            tvFollow.setTextColor(tvFollow.context.getColor(R.color.color_black_1_60))
        } else {
            tvFollow.text = tvFollow.context.getString(R.string.follow)
            tvFollow.background = tvFollow.context.getDrawable(R.drawable.bg_red_1_stroke_radius_16)
            tvFollow.setTextColor(tvFollow.context.getColor(R.color.color_red_1))
        }
    }
}