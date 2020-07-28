package com.dabenxiang.mimi.view.club

import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
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
            tvFollow.background = tvFollow.context.getDrawable(R.drawable.bg_white_1_stroke_radius_16)
            tvFollow.setTextColor(tvFollow.context.getColor(R.color.color_white_1))
        } else {
            tvFollow.text = tvFollow.context.getString(R.string.follow)
            tvFollow.background = tvFollow.context.getDrawable(R.drawable.bg_red_1_stroke_radius_16)
            tvFollow.setTextColor(tvFollow.context.getColor(R.color.color_red_1))
        }

        tvFollow.setOnClickListener {
            clubFuncItem.onFollowClick(item, !(item.isFollow ?: false)) { isFollow -> updateFollowItem(isFollow) }
        }

        if (rvPost.adapter == null || rvPost.tag != position) {
            rvPost.tag = position
            rvPost.adapter = ClubMemberPostAdapter(rvPost.context, item.posts, item, clubFuncItem)
        }

        item.avatarAttachmentId.toString().takeIf { !TextUtils.isEmpty(it) && it != LruCacheUtils.ZERO_ID }?.also { id ->
            LruCacheUtils.getLruCache(id)?.also { bitmap ->
                Glide.with(ivAvatar.context).load(bitmap).circleCrop().into(ivAvatar)
            } ?: run {
                ivAvatar.tag = id
                clubFuncItem.getBitmap(item.avatarAttachmentId.toString()) { id -> updateAvatar(id) }
            }
        } ?: run { Glide.with(ivAvatar.context).load(ivAvatar.context.getDrawable(R.drawable.icon_cs_photo)).centerCrop().into(ivAvatar) }

        clMemberPost.setOnClickListener {
            clubFuncItem.onItemClick(item)
        }
    }

    private fun updateAvatar(id: String) {
        takeIf { ivAvatar.tag == id }?.also {
            val bitmap = LruCacheUtils.getLruCache(id)
            Glide.with(ivAvatar.context).load(bitmap).circleCrop().into(ivAvatar)
        }
    }

    private fun updateFollowItem(isFollow: Boolean) {
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