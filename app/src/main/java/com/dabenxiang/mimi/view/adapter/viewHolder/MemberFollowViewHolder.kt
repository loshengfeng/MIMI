package com.dabenxiang.mimi.view.adapter.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberFollowItem
import com.dabenxiang.mimi.view.adapter.MemberFollowAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.android.synthetic.main.item_follow_member.view.*

class MemberFollowViewHolder(
    itemView: View,
    val listener: MemberFollowAdapter.EventListener
) : BaseAnyViewHolder<MemberFollowItem>(itemView) {
    private val ivPhoto: ImageView = itemView.iv_photo
    private val tvName: TextView = itemView.tv_name
    private val tvSubTitle: TextView = itemView.tv_sub_title
    private val tvFollow: TextView = itemView.tv_follow

    init {
        tvFollow.setOnClickListener { data?.userId?.let { userId -> listener.onCancelFollow(userId) } }
    }

    override fun updated(position: Int) {
        data?.avatarAttachmentId?.let {
            LruCacheUtils.getLruCache(it.toString())?.also { bitmap ->
                val options: RequestOptions = RequestOptions()
                    .transform(MultiTransformation(CenterCrop(), CircleCrop()))
                    .placeholder(R.drawable.default_profile_picture)
                    .error(R.drawable.default_profile_picture)
                    .priority(Priority.NORMAL)

                Glide.with(App.self).load(bitmap)
                    .apply(options)
                    .into(ivPhoto)
            } ?: run {
                listener.onGetAttachment(it.toString(), position)
            }
        }

        tvName.text = data?.friendlyName
        tvSubTitle.text = data?.friendlyName
    }

    override fun updated() {
    }
}