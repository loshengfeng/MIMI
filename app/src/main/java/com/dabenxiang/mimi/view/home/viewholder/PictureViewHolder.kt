package com.dabenxiang.mimi.view.home.viewholder

import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.model.api.vo.ContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils.getLruCache
import com.google.gson.Gson
import kotlinx.android.synthetic.main.nested_item_home_picture.view.*
import java.util.*

class PictureViewHolder(
    itemView: View,
    onClickListener: IndexViewHolderListener,
    private val attachmentListener: AttachmentListener
) :
    BaseIndexViewHolder<MemberPostItem>(itemView, onClickListener) {

    private val card = itemView.layout_card!!
    private val pictureImage = itemView.iv_poster!!
    private val avatarImg = itemView.img_avatar!!
    private val posterName = itemView.tv_name!!
    private val posterTime = itemView.tv_time!!
    private val title = itemView.tv_title!!

    init {
        pictureImage.setOnClickListener {
            listener.onClickItemIndex(it, index)
        }
    }

    override fun updated(model: MemberPostItem?) {
        val contentItem = Gson().fromJson(model?.content, ContentItem::class.java)
        val postImageItem =
            takeIf { contentItem.images.isNotEmpty() }?.let {
                contentItem.images.get(0)
            }

        posterName.text = model?.postFriendlyName
        posterTime.text = GeneralUtils.getTimeDiff(model?.creationDate ?: Date(), Date())
        title.text = model?.title

        card.setCardBackgroundColor(
            itemView.resources.getColor(
                R.color.adult_color_card_background,
                null
            )
        )

        postImageItem?.also {
            if (!TextUtils.isEmpty(postImageItem.url)) {
                Glide.with(itemView.context)
                    .load(postImageItem.url)
                    .into(pictureImage)
            } else {
                if (!TextUtils.isEmpty(postImageItem.id)) {
                    if (getLruCache(postImageItem.id) == null) {
                        attachmentListener.onGetAttachment(
                            postImageItem.id,
                            index,
                            AttachmentType.ADULT_HOME_PICTURE
                        )
                    } else {
                        val bitmap = getLruCache(postImageItem.id)
                        Glide.with(itemView.context)
                            .load(bitmap)
                            .into(pictureImage)
                    }
                }
            }
        } ?: run {
            Glide.with(itemView.context)
                .load(R.drawable.img_notlogin)
                .into(pictureImage)
        }

        if (getLruCache(model?.avatarAttachmentId.toString()) == null) {
            attachmentListener.onGetAttachment(
                model?.avatarAttachmentId.toString(),
                index,
                AttachmentType.ADULT_HOME_PICTURE
            )
        } else {
            val bitmap = getLruCache(model?.avatarAttachmentId.toString())
            Glide.with(itemView.context)
                .load(bitmap)
                .circleCrop()
                .into(avatarImg)
        }
    }
}