package com.dabenxiang.mimi.view.adapter.viewHolder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_clip.view.*

class ClipViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var playerView = view.player_view!!
    var coverView = view.iv_cover!!
}