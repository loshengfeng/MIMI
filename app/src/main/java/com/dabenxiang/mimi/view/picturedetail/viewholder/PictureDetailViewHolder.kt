package com.dabenxiang.mimi.view.picturedetail.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.item_picture_detail.view.*

class PictureDetailViewHolder(itemView: View) : BaseViewHolder(itemView) {

    val avatarImg: ImageView = itemView.img_avatar
    val posterName: TextView = itemView.tv_name
    val posterTime: TextView = itemView.tv_time
    val title: TextView = itemView.tv_title
    val follow: TextView = itemView.tv_follow
    val photoGrid: RecyclerView = itemView.recycler_photo
    val tagChipGroup: ChipGroup = itemView.chip_group_tag
}
