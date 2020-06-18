package com.dabenxiang.mimi.view.postfavorite

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.PlayListItem
import com.dabenxiang.mimi.view.adapter.PostFavoriteAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import com.google.android.material.chip.ChipGroup
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class PostFavoriteViewHolder(
    itemView: View,
    listener: PostFavoriteAdapter.EventListener
) : BaseAnyViewHolder<PlayListItem>(itemView), KoinComponent {
    private val gson: Gson by inject()
    private val tvTitle = itemView.findViewById(R.id.tv_title) as TextView
    private val tvIntro = itemView.findViewById(R.id.tv_intro) as TextView
    private val ivPhoto = itemView.findViewById(R.id.iv_photo) as ImageView
    private val tvLength = itemView.findViewById(R.id.tv_length) as TextView
    private val reflowGroup = itemView.findViewById(R.id.reflow_group) as ChipGroup
    private val tvFavorite = itemView.findViewById(R.id.tv_favorite) as TextView
    private val tvLike = itemView.findViewById(R.id.tv_like) as TextView
    private val tvMsg = itemView.findViewById(R.id.tv_msg) as TextView
    private val tvShare = itemView.findViewById(R.id.tv_share) as TextView
    private val tvMore = itemView.findViewById(R.id.tv_more) as TextView

    init {
        ivPhoto.setOnClickListener {
            Timber.d("tvLike")
            listener.onVideoClick(it, data!!)
        }

        tvLike.setOnClickListener {
            Timber.d("tvLike")
            listener.onLikeClick(it, data!!)
        }

        tvFavorite.setOnClickListener {
            Timber.d("tvFavorite")
            listener.onFavoriteClick(it, data!!)
        }

        tvMsg.setOnClickListener {
            Timber.d("tvMsg")
            listener.onMsgClick(it, data!!)
        }

        tvShare.setOnClickListener {
            Timber.d("tvShare")
            listener.onShareClick(it, data!!)
        }

        tvMore.setOnClickListener {
            Timber.d("tvMore")
            listener.onMoreClick(it, data!!)
        }
    }

    override fun updated() {
        Timber.d("updated")

        tvTitle.text = data?.title
        tvIntro.text = data?.description

        Glide.with(ivPhoto.context)
            .load(data?.cover)
            .into(ivPhoto)

        // todo: no data...
        tvLength.text = "09:00:00"
        tvFavorite.text = data?.favoriteCount.toString()
        tvLike.text = data?.likeCount.toString()

//        tvTitle.text = data?.title
//        ivPhoto.setImageResource(R.drawable.img_nopic_03)
//
//        val contentItem = gson.fromJson(data?.content.toString(), ContentItem::class.java)
//        tvLength.text = contentItem?.shortVideoItem?.length
//
//        tvFavorite.text = data?.favoriteCount.toString()
//        tvLike.text = data?.likeCount.toString()
    }
}