package com.dabenxiang.mimi.view.actor

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_actor_video.view.*

class ActorVideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val title: TextView = itemView.tv_title
    val image: ImageView = itemView.iv_cover
    val item: ConstraintLayout = itemView.cl_actor_video
}