package com.dabenxiang.mimi.view.actor

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_actor_categories.view.*
import kotlinx.android.synthetic.main.item_actor_videos.view.*
import kotlinx.android.synthetic.main.item_actor_videos.view.tv_name

class ActorCategoriesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val name: TextView = itemView.tv_name
    val ivAvatar: ImageView = itemView.iv_photo
    val item: ConstraintLayout = itemView.cl_category
}