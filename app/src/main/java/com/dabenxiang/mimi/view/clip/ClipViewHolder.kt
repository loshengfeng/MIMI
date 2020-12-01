package com.dabenxiang.mimi.view.clip

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.android.synthetic.main.item_clip.view.*
import kotlinx.android.synthetic.main.recharge_reminder.view.*

class ClipViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    var playerView: PlayerView = view.player_view
    var ivCover: ImageView = view.iv_cover
    var ibReplay: ImageButton = view.ib_replay
    var ibPlay: ImageButton = view.ib_play
    var tvTitle: TextView = view.tv_title
    var tvFavorite: TextView = view.iv_favorite
    var tvComment: TextView = view.tv_comment
    var tvMore: TextView = view.tv_more
    var progress: ProgressBar = view.progress_video
    var reminder: View = view.recharge_reminder
    private var btnVip: View = view.btn_vip
    private var btnPromote: View = view.btn_promote

    fun onBind(item: VideoItem) {
        reminder.visibility = View.GONE
        ibReplay.visibility = View.GONE
        ibPlay.visibility = View.GONE
        tvTitle.text = item.title
        tvFavorite.text = item.favoriteCount.toString()
        tvComment.text = item.commentCount.toString()

        Glide.with(ivCover.context)
            .load(item.cover).placeholder(R.drawable.img_nopic_03).into(ivCover)

        val favoriteRes = takeIf { item.favorite == true }?.let { R.drawable.btn_favorite_forvideo_s }
            ?: let { R.drawable.btn_favorite_forvideo_n }
        tvFavorite.setCompoundDrawablesRelativeWithIntrinsicBounds(0, favoriteRes, 0, 0)
    }

    fun updateAfterM3U8(item: VideoItem, clipFuncItem: ClipFuncItem, pos: Int, isOverdue: Boolean) {
        if (isOverdue) {
            btnVip.setOnClickListener {
                clipFuncItem.onVipClick()
            }
            btnPromote.setOnClickListener {
                clipFuncItem.onPromoteClick()
            }
            reminder.visibility = View.VISIBLE
            progress.visibility = View.GONE
        } else {
            tvFavorite.setOnClickListener {
                clipFuncItem.onFavoriteClick(item, pos, item.favorite != true)
            }
            tvComment.setOnClickListener { clipFuncItem.onCommentClick(item) }
            tvMore.setOnClickListener { clipFuncItem.onMoreClick(item) }

            reminder.visibility = View.GONE
        }
    }
}