package com.dabenxiang.mimi.view.picturepost

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_picture_post.view.*

class PicturePostHolder(itemView: View) : BaseViewHolder(itemView) {

    val avatarImg: ImageView = itemView.img_avatar
    val name: TextView = itemView.tv_name
    val time: TextView = itemView.tv_time
    val follow: TextView = itemView.tv_follow


}