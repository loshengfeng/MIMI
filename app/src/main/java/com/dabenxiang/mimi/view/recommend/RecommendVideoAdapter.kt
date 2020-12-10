package com.dabenxiang.mimi.view.recommend

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.dabenxiang.mimi.JOEY
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.RecommendVideoItem
import kotlinx.android.synthetic.main.item_recommend_video.view.*

class RecommendVideoAdapter(
    private val videos: List<RecommendVideoItem>,
    private val recommendFuncItem: RecommendFuncItem
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val mView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommend_video, parent, false)
        return RecommendVideoViewHolder(mView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as RecommendVideoViewHolder

        val video = videos[position]

        val options = RequestOptions()
            .priority(Priority.NORMAL)
            .placeholder(R.drawable.img_nopic_03)
            .error(R.drawable.img_nopic_03)

        Glide.with(holder.videoImage.context).load(video.cover)
            .apply(options)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    recommendFuncItem.getDecryptSetting(JOEY)?.takeIf { it.isImageDecrypt }
                        ?.let { decryptSettingItem ->
                            recommendFuncItem.decryptCover(video.cover, decryptSettingItem) {
                                Glide.with(holder.videoImage.context).load(it)
                                    .apply(options)
                                    .into(holder.videoImage)
                            }
                        }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
            .into(holder.videoImage)

        holder.videoTitleText.text = video.title
        holder.videoLayout.setOnClickListener { recommendFuncItem.onItemClick(video) }
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    class RecommendVideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var videoImage: ImageView = itemView.iv_video
        val videoTitleText: TextView = itemView.tv_title
        val videoLayout: ConstraintLayout = itemView.layout_video
    }
}