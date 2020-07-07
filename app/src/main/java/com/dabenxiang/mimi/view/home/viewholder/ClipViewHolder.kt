package com.dabenxiang.mimi.view.home.viewholder

import android.view.View
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.error.PostContentItem
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.google.gson.Gson
import kotlinx.android.synthetic.main.nested_item_home_clip.view.*

class ClipViewHolder(itemView: View, onClickListener: BaseIndexViewHolder.IndexViewHolderListener) :
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

        Glide.with(itemView.context)
            .load(postContentItem.images[0])
            .into(videoImage)

        videoTime.text = postContentItem.length

        profileName.text = ""
        profileTime.text = model?.creationDate
        title.text = model?.title

        card.setCardBackgroundColor(itemView.resources.getColor(R.color.adult_color_card_background, null))

//        val creatorId = model?.creatorId

    }

}