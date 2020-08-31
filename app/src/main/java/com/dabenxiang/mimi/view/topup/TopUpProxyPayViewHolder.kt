package com.dabenxiang.mimi.view.topup

import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.AgentItem
import com.dabenxiang.mimi.view.adapter.TopUpAgentAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.android.synthetic.main.item_topup_proxy_pay.view.*
import timber.log.Timber

class TopUpProxyPayViewHolder(
    view: View,
    val listener: TopUpAgentAdapter.EventListener
) : BaseAnyViewHolder<AgentItem>(view) {
    private val ivPhoto = itemView.iv_photo
    private val tvSubtitle = itemView.tv_subtitle

    init {
        view.setOnClickListener { data?.let { item -> listener.onItemClick(view, item) } }
    }

    override fun updated() {
        Timber.d("${TopUpProxyPayViewHolder::class.java.simpleName}_updated")
        tvSubtitle.text = data?.merchantName

        data?.avatarAttachmentId?.let { avatarId ->
            LruCacheUtils.getLruArrayCache(avatarId)?.also { array ->
                val options: RequestOptions = RequestOptions()
                        .transform(MultiTransformation(CenterCrop(), CircleCrop()))
                        .placeholder(R.drawable.default_profile_picture)
                        .error(R.drawable.default_profile_picture)
                        .priority(Priority.NORMAL)

                Glide.with(App.self).asBitmap()
                        .load(array)
                        .apply(options)
                        .into(ivPhoto)
            } ?: takeIf { avatarId != LruCacheUtils.ZERO_ID }?.run {
                listener.onGetAvatarAttachment(avatarId, adapterPosition)
            }
        }
    }
}