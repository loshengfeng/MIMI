package com.dabenxiang.mimi.view.home.viewholder

import android.graphics.Bitmap
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.error.PostContentItem
import com.dabenxiang.mimi.model.enums.HomeItemType
import com.dabenxiang.mimi.view.adapter.HomeClipAdapter
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.google.gson.Gson
import kotlinx.android.synthetic.main.nested_item_home_clip.view.*

class ClipViewHolder(
    itemView: View,
    onClickListener: IndexViewHolderListener,
    private val onClipListener: HomeClipAdapter.ClipListener,
    private val attachmentMap: HashMap<Long, Bitmap>
) :
    BaseIndexViewHolder<MemberPostItem>(itemView, onClickListener) {

    private val card = itemView.layout_card!!
    private val videoImage = itemView.iv_poster!!
    private val videoTime = itemView.tv_video_time!!
    private val profileImg = itemView.img_profile!!
    private val profileName = itemView.tv_name!!
    private val profileTime = itemView.tv_time!!
    private val title = itemView.tv_title!!

    init {
        videoImage.setOnClickListener {
            listener.onClickItemIndex(it, index)
        }
    }

    override fun updated(model: MemberPostItem?) {
        val postContentItem = Gson().fromJson(model?.content, PostContentItem::class.java)

        val postImageItem = postContentItem.images[0]

        if (!TextUtils.isEmpty(postImageItem.url)) {
            Glide.with(itemView.context)
                .load(postImageItem.url)
                .into(videoImage)
        } else {
            if (!TextUtils.isEmpty(postImageItem.id)) {
                if (attachmentMap[postImageItem.id.toLong()] == null) {
                    onClipListener.onGetVideoImg(
                        postImageItem.id.toLong(),
                        index,
                        HomeItemType.CLIP
                    )

                } else {
                    val bitmap = attachmentMap[postImageItem.id.toLong()]
                    Glide.with(itemView.context)
                        .load(bitmap)
                        .into(videoImage)
                }
            }
        }

        videoTime.text = postContentItem.shortVideo.length

        profileName.text = model?.creatorId.toString()
        profileTime.text = model?.creationDate
        title.text = model?.title

        card.setCardBackgroundColor(
            itemView.resources.getColor(
                R.color.adult_color_card_background,
                null
            )
        )

        if (attachmentMap[model?.avatarAttachmentId] == null) {
            onClipListener.onGetAvatar(model?.avatarAttachmentId!!, index, HomeItemType.CLIP)
        } else {
            val bitmap = attachmentMap[model?.avatarAttachmentId]
            Glide.with(itemView.context)
                .load(bitmap)
                .circleCrop()
                .into(profileImg)
        }
    }
}