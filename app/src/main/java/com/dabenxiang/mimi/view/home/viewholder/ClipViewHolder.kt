package com.dabenxiang.mimi.view.home.viewholder

import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.model.api.vo.ContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.HomeItemType
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils.getLruCache
import com.google.gson.Gson
import kotlinx.android.synthetic.main.nested_item_home_clip.view.*
import java.util.*

class ClipViewHolder(
    itemView: View,
    onClickListener: IndexViewHolderListener,
    private val attachmentListener: AttachmentListener
) :
    BaseIndexViewHolder<MemberPostItem>(itemView, onClickListener) {

    private val card = itemView.layout_card!!
    private val videoImage = itemView.iv_poster!!
    private val videoTime = itemView.tv_video_time!!
    private val avatarImg = itemView.img_avatar!!
    private val profileName = itemView.tv_name!!
    private val profileTime = itemView.tv_time!!
    private val title = itemView.tv_title!!

    init {
        videoImage.setOnClickListener {
            listener.onClickItemIndex(it, index)
        }
    }

    override fun updated(model: MemberPostItem?) {
        val contentItem = Gson().fromJson(model?.content, ContentItem::class.java)
        val postImageItem = contentItem.images[0]

        videoTime.text = contentItem.shortVideo.length
        profileName.text = model?.postFriendlyName
        profileTime.text = GeneralUtils.getTimeDiff(model?.creationDate ?: Date(), Date())
        title.text = model?.title

        card.setCardBackgroundColor(
            itemView.resources.getColor(
                R.color.adult_color_card_background,
                null
            )
        )

        if (!TextUtils.isEmpty(postImageItem.url)) {
            Glide.with(itemView.context)
                .load(postImageItem.url)
                .into(videoImage)
        } else {
            if (!TextUtils.isEmpty(postImageItem.id)) {
                if (getLruCache(postImageItem.id.toLong()) == null) {
                    attachmentListener.onGetAttachment(
                        postImageItem.id.toLong(),
                        index,
                        HomeItemType.CLIP
                    )
                } else {
                    val bitmap = getLruCache(postImageItem.id.toLong())
                    Glide.with(itemView.context)
                        .load(bitmap)
                        .into(videoImage)
                }
            }
        }

        if (getLruCache(model?.avatarAttachmentId!!) == null) {
            attachmentListener.onGetAttachment(
                model.avatarAttachmentId,
                index,
                HomeItemType.CLIP
            )
        } else {
            val bitmap = getLruCache(model?.avatarAttachmentId)
            Glide.with(itemView.context)
                .load(bitmap)
                .circleCrop()
                .into(avatarImg)
        }
    }
}