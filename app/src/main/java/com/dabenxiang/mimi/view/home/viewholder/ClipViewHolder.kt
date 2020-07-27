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
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils.ZERO_ID
import com.dabenxiang.mimi.widget.utility.LruCacheUtils.getLruCache
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
        val postImageItem = takeIf { contentItem.images != null && contentItem.images.isNotEmpty() }?.let { contentItem.images?.get(0) }

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

        postImageItem?.also {
            if (!TextUtils.isEmpty(postImageItem.url)) {
                Glide.with(itemView.context)
                    .load(postImageItem.url).placeholder(R.drawable.img_nopic_03).into(videoImage)
            } else {
                if (!TextUtils.isEmpty(postImageItem.id) && postImageItem.id != ZERO_ID) {
                    val imageId = postImageItem.id
                    if (getLruCache(imageId) == null) {
                        memberPostFuncItem.getBitmap(imageId) { id -> updateImage(id) }
                    } else {
                        updateImage(imageId)
                    }
                } else {
                    Glide.with(itemView.context).load(R.drawable.img_nopic_03).into(videoImage)
                }
            }
        } ?: run {
            Glide.with(itemView.context).load(R.drawable.img_nopic_03).into(videoImage)
        }


        val avatarId = model?.avatarAttachmentId.toString()
        if (getLruCache(model?.avatarAttachmentId.toString()) == null) {
            memberPostFuncItem.getBitmap(avatarId) { id -> updateAvatar(id) }
        } else {
            updateAvatar(avatarId)
        }
    }

    private fun updateImage(id: String) {
        val bitmap = getLruCache(id)
        Glide.with(videoImage.context).load(bitmap).into(videoImage)
    }

    private fun updateAvatar(id: String) {
        val bitmap = getLruCache(id)
        Glide.with(avatarImg.context).load(bitmap).circleCrop().into(avatarImg)
    }
}