package com.dabenxiang.mimi.view.clip

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_clip.view.*

class ClipViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var playerView = view.player_view!!
    var ivCover = view.iv_cover!!
    var ibReplay = view.ib_replay!!
    var ibBack = view.ib_back!!
    var tvTitle = view.tv_title!!
    var cgTag = view.cg_tag!!
}