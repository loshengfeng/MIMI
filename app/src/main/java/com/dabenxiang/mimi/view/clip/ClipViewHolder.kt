package com.dabenxiang.mimi.view.clip

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.android.synthetic.main.item_clip.view.*

class ClipViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var playerView: PlayerView = view.player_view
    var ivCover: ImageView = view.iv_cover
    var ibReplay: ImageButton = view.ib_replay
    var ibBack: ImageButton = view.ib_back
    var tvTitle: TextView = view.tv_title
    var tvName: TextView = view.tv_name
    var tvFavorite: TextView = view.tv_favorite
    var tvLike: TextView = view.tv_like
    var tvComment: TextView = view.tv_comment

    fun onBind(item: MemberPostItem) {
        ibReplay.visibility = View.GONE

        tvTitle.text = item.title
        tvName.text = "@${item.postFriendlyName}"
        tvFavorite.text = item.favoriteCount.toString()
        tvLike.text = item.likeCount.toString()
        tvComment.text = item.commentCount.toString()
    }
}