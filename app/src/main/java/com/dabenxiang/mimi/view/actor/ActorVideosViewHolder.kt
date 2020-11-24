package com.dabenxiang.mimi.view.actor

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_actor_videos.view.*

class ActorVideosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val name: TextView = itemView.tv_name
    val totalClick: TextView = itemView.tv_total_click
    val totalVideo: TextView = itemView.tv_total_video
    val ivAvatar: ImageView = itemView.iv_avatar
    val actressesVideos: RecyclerView = itemView.rv_actresses_videos
}