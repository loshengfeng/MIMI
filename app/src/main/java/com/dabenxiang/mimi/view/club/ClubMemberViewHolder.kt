package com.dabenxiang.mimi.view.club

import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_club_member.view.*

/**
 * VAI4.1.6_圈子頁
 */
class ClubMemberViewHolder(view: View) : BaseViewHolder(view) {
    val ivAvatar = view.iv_avatar
    val tvTitle = view.tv_title
    val tvDesc = view.tv_desc
    val tvFollowCount = view.tv_follow_count
    val tvPostCount = view.tv_post_count
    val tvFollow = view.tv_follow
    val rvPost = view.rv_post

    fun onBind(item: MemberClubItem, clubFuncItem: ClubFuncItem) {
        tvTitle.text = item.title
        tvDesc.text = item.description
        tvFollowCount.text = item.followerCount.toString()
        tvPostCount.text = item.postCount.toString()
        updateFollowItem(item)

        rvPost.adapter = ClubMemberPostAdapter(rvPost.context, item.posts, clubFuncItem)
    }

    private fun updateFollowItem(item: MemberClubItem) {
        val isFollow = item.isFollow ?: false
        if (isFollow) {
            tvFollow.text = tvFollow.context.getString(R.string.followed)
            tvFollow.background = tvFollow.context.getDrawable(R.drawable.bg_white_1_stroke_radius_16)
            tvFollow.setTextColor(tvFollow.context.getColor(R.color.color_white_1))
        } else {
            tvFollow.text = tvFollow.context.getString(R.string.follow)
            tvFollow.background = tvFollow.context.getDrawable(R.drawable.bg_red_1_stroke_radius_16)
            tvFollow.setTextColor(tvFollow.context.getColor(R.color.color_red_1))
        }
    }
}