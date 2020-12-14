package com.dabenxiang.mimi.view.club.pic

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_photo_grid.view.*

class PictureGridViewHolder(itemView: View) : BaseViewHolder(itemView) {
    val cardView: CardView = itemView.layout_card
    val picture: ImageView = itemView.iv_picture
    val mask: View = itemView.view_mask
    val imageCount: TextView = itemView.tv_image_count
}
