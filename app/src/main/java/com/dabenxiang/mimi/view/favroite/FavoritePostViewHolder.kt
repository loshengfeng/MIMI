package com.dabenxiang.mimi.view.favroite

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.Gson
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ContentItem
import com.dabenxiang.mimi.model.api.vo.PostFavoriteItem
import com.dabenxiang.mimi.view.adapter.FavoriteAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import com.google.android.material.chip.ChipGroup
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.text.SimpleDateFormat
import java.util.*

class FavoritePostViewHolder(
    itemView: View,
    listener: FavoriteAdapter.EventListener
) : BaseAnyViewHolder<PostFavoriteItem>(itemView), KoinComponent {
    private val gson: Gson by inject()
    private val ivHead = itemView.findViewById(R.id.iv_head) as ImageView
    private val tvName = itemView.findViewById(R.id.tv_name) as TextView
    private val tvTime = itemView.findViewById(R.id.tv_time) as TextView
    private val tvFollow = itemView.findViewById(R.id.tv_follow) as TextView
    private val tvTitle = itemView.findViewById(R.id.tv_title) as TextView
    private val ivPhoto = itemView.findViewById(R.id.iv_photo) as ImageView
    private val tvLength = itemView.findViewById(R.id.tv_length) as TextView
    private val reflowGroup = itemView.findViewById(R.id.reflow_group) as ChipGroup
    private val tvFavorite = itemView.findViewById(R.id.tv_favorite) as TextView
    private val tvLike = itemView.findViewById(R.id.tv_like) as TextView
    private val tvMsg = itemView.findViewById(R.id.tv_msg) as TextView
    private val tvShare = itemView.findViewById(R.id.tv_share) as TextView
    private val tvMore = itemView.findViewById(R.id.tv_more) as TextView

    init {
        ivPhoto.setOnClickListener { listener.onFunctionClick(FavoriteAdapter.FunctionType.Video, it, data!!) }
        tvLike.setOnClickListener { listener.onFunctionClick(FavoriteAdapter.FunctionType.Like, it, data!!) }
        tvFavorite.setOnClickListener { listener.onFunctionClick(FavoriteAdapter.FunctionType.Favorite, it, data!!) }
        tvMsg.setOnClickListener { listener.onFunctionClick(FavoriteAdapter.FunctionType.Msg, it, data!!) }
        tvShare.setOnClickListener { listener.onFunctionClick(FavoriteAdapter.FunctionType.Share, it, data!!) }
        tvMore.setOnClickListener { listener.onFunctionClick(FavoriteAdapter.FunctionType.More, it, data!!) }
    }

    override fun updated() {
        tvName.text = data?.posterName
        tvTitle.text = data?.title
        tvTime.text = data?.postDate.let { date -> SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date) }
        val contentItem = gson.fromJson(data?.content.toString(), ContentItem::class.java)
        tvLength.text = contentItem?.shortVideoItem?.length

        // todo:...
//        Glide.with(ivPhoto.context)
//            .load(contentItem.images?.get(0))
//            .into(ivPhoto)

        tvLike.text = data?.likeCount.toString()
        tvFavorite.text = data?.favoriteCount.toString()
        tvMsg.text = data?.commentCount.toString()
    }
}