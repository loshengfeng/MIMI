package com.dabenxiang.mimi.view.home.viewholder

import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.model.api.vo.MediaContentItem
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
    private val memberPostFuncItem: MemberPostFuncItem
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
        val contentItem = Gson().fromJson(model?.content, MediaContentItem::class.java)
        val postImageItem =
            takeIf { contentItem.images?.isNotEmpty() ?: false }?.let {
                contentItem.images?.get(0)
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
                    val imageId = postImageItem.id
                    if (getLruCache(imageId) == null) {
                        memberPostFuncItem.getBitmap(imageId) { id -> updateImage(id) }
                    } else {
                        updateImage(imageId)
                    }
                }
            }
        } ?: run {
            Glide.with(itemView.context)
                .load(R.drawable.img_404)
                .into(pictureImage)
        }

        val avatarId = model?.avatarAttachmentId.toString()
        if (getLruCache(avatarId) == null) {
            memberPostFuncItem.getBitmap(avatarId) { id -> updateAvatar(id) }
        } else {
            updateAvatar(avatarId)
        }
    }

    private fun updateImage(id: String) {
        val bitmap = getLruCache(id)
        Glide.with(pictureImage.context).load(bitmap).into(pictureImage)
    }

    private fun updateAvatar(id: String) {
        val bitmap = getLruCache(id)
        Glide.with(avatarImg.context).load(bitmap).circleCrop().into(avatarImg)
    }
}