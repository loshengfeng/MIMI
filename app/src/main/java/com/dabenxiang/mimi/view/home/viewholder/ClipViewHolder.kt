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
import kotlinx.android.synthetic.main.nested_item_home_clip.view.*
import timber.log.Timber
import java.util.*

class ClipViewHolder(
    itemView: View,
    onClickListener: IndexViewHolderListener,
    private val memberPostFuncItem: MemberPostFuncItem
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
        Timber.d("content: ${model?.content}")
        val contentItem = Gson().fromJson(model?.content, MediaContentItem::class.java)
        val postImageItem =
            takeIf { contentItem.images != null && contentItem.images.isNotEmpty() }?.let {
                contentItem.images?.get(0)
            }

        videoTime.text = contentItem.shortVideo?.length
        profileName.text = model?.postFriendlyName
        profileTime.text = GeneralUtils.getTimeDiff(model?.creationDate ?: Date(), Date())
        title.text = model?.title

        card.setCardBackgroundColor(
            itemView.resources.getColor(
                R.color.adult_color_card_background,
                null
            )
        )

        memberPostFuncItem.getBitmap(
            postImageItem?.id?.toLongOrNull(),
            videoImage,
            LoadImageType.THUMBNAIL
        )
        memberPostFuncItem.getBitmap(model?.avatarAttachmentId, avatarImg, LoadImageType.AVATAR)
    }

}