package com.dabenxiang.mimi.view.textdetail.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.item_text_detail.view.*

class TextDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val avatarImg: ImageView = itemView.img_avatar
    val posterName: TextView = itemView.tv_name
    val posterTime: TextView = itemView.tv_time
    val title: TextView = itemView.tv_title
    val desc: TextView = itemView.tv_text_desc
    val follow: TextView = itemView.tv_follow
    val tagChipGroup: ChipGroup = itemView.chip_group_tag

}