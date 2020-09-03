package com.dabenxiang.mimi.view.home.viewholder

import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.model.api.vo.MediaContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
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

        memberPostFuncItem.getBitmap(postImageItem?.id?.toLongOrNull(), pictureImage, LoadImageType.PICTURE_THUMBNAIL)

        memberPostFuncItem.getBitmap(model?.avatarAttachmentId, avatarImg, LoadImageType.AVATAR)
    }

}