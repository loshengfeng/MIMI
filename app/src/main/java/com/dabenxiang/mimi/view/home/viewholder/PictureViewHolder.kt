package com.dabenxiang.mimi.view.home.viewholder

import android.graphics.Bitmap
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.model.api.vo.ContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.HomeItemType
import com.dabenxiang.mimi.view.adapter.HomeAdapter
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.gson.Gson
import kotlinx.android.synthetic.main.nested_item_home_picture.view.*
import java.util.*

class PictureViewHolder(
    itemView: View,
    onClickListener: IndexViewHolderListener,
    private val attachmentListener: AttachmentListener,
    private val attachmentMap: HashMap<Long, Bitmap>
) :
    BaseIndexViewHolder<MemberPostItem>(itemView, onClickListener) {

    private val card = itemView.layout_card!!
    private val pictureImage = itemView.iv_poster!!
    private val avatarImg = itemView.img_avatar!!
    private val profileName = itemView.tv_name!!
    private val profileTime = itemView.tv_time!!
    private val title = itemView.tv_title!!

    init {
        pictureImage.setOnClickListener {
            listener.onClickItemIndex(it, index)
        }
    }

    override fun updated(model: MemberPostItem?) {
        val contentItem = Gson().fromJson(model?.content, ContentItem::class.java)
        val postImageItem = contentItem.images[0]

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
                .into(pictureImage)
        } else {
            if (!TextUtils.isEmpty(postImageItem.id)) {
                if (attachmentMap[postImageItem.id.toLong()] == null) {
                    attachmentListener.onGetAttachment(
                        postImageItem.id.toLong(),
                        index,
                        HomeItemType.PICTURE
                    )
                } else {
                    val bitmap = attachmentMap[postImageItem.id.toLong()]
                    Glide.with(itemView.context)
                        .load(bitmap)
                        .into(pictureImage)
                }
            }
        }

        if (attachmentMap[model?.avatarAttachmentId] == null) {
            attachmentListener.onGetAttachment(
                model?.avatarAttachmentId!!,
                index,
                HomeItemType.CLIP
            )
        } else {
            val bitmap = attachmentMap[model?.avatarAttachmentId]
            Glide.with(itemView.context)
                .load(bitmap)
                .circleCrop()
                .into(avatarImg)
        }
    }
}