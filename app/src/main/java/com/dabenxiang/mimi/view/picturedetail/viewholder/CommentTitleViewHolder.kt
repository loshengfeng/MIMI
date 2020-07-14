package com.dabenxiang.mimi.view.picturedetail.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_comment_title.view.*

class CommentTitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val newestComment: TextView = itemView.tv_comment_newest
    val topComment: TextView = itemView.tv_comment_top
}
