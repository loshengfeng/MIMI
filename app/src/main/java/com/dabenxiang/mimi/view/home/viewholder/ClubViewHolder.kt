package com.dabenxiang.mimi.view.home.viewholder

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ContentItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.enums.HomeItemType
import com.dabenxiang.mimi.view.adapter.HomeAdapter
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.google.gson.Gson
import kotlinx.android.synthetic.main.nested_item_home_club.view.*

class ClubViewHolder(
    itemView: View,
    onClickListener: IndexViewHolderListener,
    val context: Context,
    private val attachmentListener: HomeAdapter.AttachmentListener,
    private val attachmentMap: HashMap<Long, Bitmap>
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

        val postItem = model?.posts?.get(0)
        val contentItem = Gson().fromJson(postItem?.content, ContentItem::class.java)

        clubName.text = model?.title
        clubDesc.text = model?.description
        followCount.text = model?.followerCount.toString()
        postCount.text = model?.postCount.toString()
        title.text = postItem?.title

        val isFollow = model?.isFollow ?: false
        if (isFollow) {
            follow.text = context.getString(R.string.club_followed)
            follow.background = context.getDrawable(R.drawable.bg_white_1_stroke_radius_16)
            follow.setTextColor(context.getColor(R.color.color_white_1))
        } else {
            follow.text = context.getString(R.string.club_follow)
            follow.background = context.getDrawable(R.drawable.bg_red_1_stroke_radius_16)
            follow.setTextColor(context.getColor(R.color.color_red_1))
        }

        if (attachmentMap[model?.avatarAttachmentId] == null) {
            attachmentListener.onGetAttachment(
                model?.avatarAttachmentId!!,
                index,
                HomeItemType.CLUB
            )
        } else {
            val bitmap = attachmentMap[model?.avatarAttachmentId]
            Glide.with(itemView.context)
                .load(bitmap)
                .circleCrop()
                .into(avatarImg)
        }

        val imageItem = contentItem?.images?.get(0)
        if (!TextUtils.isEmpty(imageItem?.url)) {
            Glide.with(itemView.context)
                .load(imageItem?.url)
                .circleCrop()
                .into(clubImg)
        } else {
            if (attachmentMap[imageItem?.id?.toLong()] == null) {
                attachmentListener.onGetAttachment(
                    imageItem?.id!!.toLong(),
                    index,
                    HomeItemType.CLUB
                )
            } else {
                val bitmap = attachmentMap[imageItem?.id?.toLong()]
                Glide.with(itemView.context)
                    .load(bitmap)
                    .into(clubImg)
            }
        }


    }
}