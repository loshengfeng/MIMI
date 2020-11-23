package com.dabenxiang.mimi.view.recommend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.RecommendVideoItem
import kotlinx.android.synthetic.main.item_recommend_video.view.*

class RecommendVideoAdapter(
    private val videos: List<RecommendVideoItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val mView =
            LayoutInflater.from(parent.context)
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
            .into(holder.videoImage)

        holder.videoTitleText.text = video.title
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    class RecommendVideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var videoImage: ImageView = itemView.iv_video
        val videoTitleText: TextView = itemView.tv_title
    }

}