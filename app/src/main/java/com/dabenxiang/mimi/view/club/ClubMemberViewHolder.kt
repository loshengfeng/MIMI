package com.dabenxiang.mimi.view.club

import android.graphics.Bitmap
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.android.synthetic.main.item_club_member.view.*
import timber.log.Timber

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

    fun onBind(item: MemberClubItem, clubFuncItem: ClubFuncItem, position: Int) {
        Timber.d("@@onBind, pos: $position, avatarId: ${item.avatarAttachmentId}")
        tvTitle.text = item.title
        tvDesc.text = item.description
        tvFollowCount.text = item.followerCount.toString()
        tvPostCount.text = item.postCount.toString()
        updateFollowItem(item)

        if (rvPost.adapter == null || rvPost.tag != position) {
            rvPost.tag = position
            rvPost.adapter = ClubMemberPostAdapter(rvPost.context, item.posts, clubFuncItem)
        }

        item.avatarAttachmentId.toString().takeIf { !TextUtils.isEmpty(it) && it.toLong() != 0L }?.also { id ->
            LruCacheUtils.getLruCache(id)?.also { bitmap ->
                Glide.with(ivAvatar.context).load(bitmap).circleCrop().into(ivAvatar)
            } ?: run {
                ivAvatar.tag = id
                clubFuncItem.getBitmap(item.avatarAttachmentId.toString()) { id -> updateAvatar(id) }
            }
        } ?: run { Glide.with(ivAvatar.context).load(ivAvatar.context.getDrawable(R.drawable.icon_cs_photo)).centerCrop().into(ivAvatar) }
    }

    private fun updateAvatar(id: String) {
        Timber.d("@@updateAvatar id: $id")
        takeIf { ivAvatar.tag == id }?.also {
            val bitmap = LruCacheUtils.getLruCache(id)
            Glide.with(ivAvatar.context).load(bitmap).circleCrop().into(ivAvatar)
        }
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