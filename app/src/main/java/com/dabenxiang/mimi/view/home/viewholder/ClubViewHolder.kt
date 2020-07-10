package com.dabenxiang.mimi.view.home.viewholder

import android.content.Context
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.model.api.vo.ContentItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.view.adapter.HomeClubAdapter
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.widget.utility.LruCacheUtils.getLruCache
import com.google.gson.Gson
import kotlinx.android.synthetic.main.nested_item_home_club.view.*

class ClubViewHolder(
    itemView: View,
    onClickListener: IndexViewHolderListener,
    val context: Context,
    private val clubListener: HomeClubAdapter.ClubListener,
    private val attachmentListener: AttachmentListener
) :
    BaseIndexViewHolder<MemberClubItem>(itemView, onClickListener) {

    private val card = itemView.layout_card!!

    private val avatarImg = itemView.img_avatar!!
    private val clubName = itemView.tv_club_name
    private val clubDesc = itemView.tv_club_desc
    private val followCount = itemView.tv_club_follow_count
    private val postCount = itemView.tv_club_post_count
    private val clubImg = itemView.img_club
    private val title = itemView.tv_title
    private val follow = itemView.tv_follow

    init {
        card.setOnClickListener {
            listener.onClickItemIndex(it, index)
        }
    }

    override fun updated(model: MemberClubItem?) {

        clubName.text = model?.title
        clubDesc.text = model?.description
        followCount.text = model?.followerCount.toString()
        postCount.text = model?.postCount.toString()

        val isFollow = model?.isFollow ?: false

        if (isFollow) {
            follow.text = context.getString(R.string.followed)
            follow.background = context.getDrawable(R.drawable.bg_white_1_stroke_radius_16)
            follow.setTextColor(context.getColor(R.color.color_white_1))
        } else {
            follow.text = context.getString(R.string.follow)
            follow.background = context.getDrawable(R.drawable.bg_red_1_stroke_radius_16)
            follow.setTextColor(context.getColor(R.color.color_red_1))
        }

        follow.setOnClickListener {
            if (isFollow) {
                clubListener.cancelFollowClub(model?.id?.toInt()!!, index)
            } else {
                clubListener.followClub(model?.id?.toInt()!!, index)
            }
        }

        if (getLruCache(model?.avatarAttachmentId.toString()) == null) {
            attachmentListener.onGetAttachment(
                model?.avatarAttachmentId.toString(),
                index,
                AttachmentType.ADULT_HOME_CLUB
            )
        } else {
            val bitmap = getLruCache(model?.avatarAttachmentId.toString())
            Glide.with(itemView.context)
                .load(bitmap)
                .circleCrop()
                .into(avatarImg)
        }

        val posts = model?.posts ?: arrayListOf()
        if (posts.isNotEmpty()) {
            val postItem = posts[0]
            title.text = postItem.title

            val contentItem = Gson().fromJson(postItem.content, ContentItem::class.java)
            val imageItem = contentItem?.images?.get(0)
            if (!TextUtils.isEmpty(imageItem?.url)) {
                Glide.with(itemView.context)
                    .load(imageItem?.url)
                    .circleCrop()
                    .into(clubImg)
            } else {
                if (getLruCache(imageItem?.id.toString()) == null) {
                    attachmentListener.onGetAttachment(
                        imageItem?.id.toString(),
                        index,
                        AttachmentType.ADULT_HOME_CLUB
                    )
                } else {
                    val bitmap = getLruCache(imageItem?.id.toString())
                    Glide.with(itemView.context)
                        .load(bitmap)
                        .into(clubImg)
                }
            }
        } else {
            Glide.with(itemView.context)
                .load(R.drawable.img_notlogin)
                .circleCrop()
                .into(clubImg)
        }

    }
}