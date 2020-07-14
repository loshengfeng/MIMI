package com.dabenxiang.mimi.view.clip

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LikeType
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

    fun onBind(item: MemberPostItem, clipFuncItem: ClipFuncItem, pos: Int) {
        ibReplay.visibility = View.GONE

        tvTitle.text = item.title
        tvName.text = "@${item.postFriendlyName}"
        tvFavorite.text = item.favoriteCount.toString()
        tvLike.text = item.likeCount.toString()
        tvComment.text = item.commentCount.toString()

        val likeRes = if (item.likeType == LikeType.LIKE) { R.drawable.ico_nice_forvideo_s } else { R.drawable.ico_nice_forvideo }
        tvLike.setCompoundDrawablesRelativeWithIntrinsicBounds(0, likeRes, 0, 0)
        val isLike= item.likeType == LikeType.LIKE
        tvLike.setOnClickListener { clipFuncItem.onLikeClick(item, pos, !isLike) }

        val favoriteRes = if (item.isFavorite) R.drawable.btn_favorite_forvideo_s else R.drawable.btn_favorite_forvideo_n
        tvFavorite.setCompoundDrawablesRelativeWithIntrinsicBounds(0, favoriteRes, 0, 0)
        tvFavorite.setOnClickListener { clipFuncItem.onFavoriteClick(item, pos, !item.isFavorite) }

        ibBack.setOnClickListener { clipFuncItem.onBackClick() }


    }
}