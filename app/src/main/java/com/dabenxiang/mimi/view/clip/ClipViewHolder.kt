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
    var tvLike: TextView = view.tv_like
    var tvComment: TextView = view.tv_comment
    var progress: ProgressBar = view.progress_video
    var reminder: View = view.recharge_reminder
    var reminder_btn_vip: View = view.btn_vip
    var reminder_btn_promote: View = view.btn_promote

    fun onBind(item: VideoItem, clipFuncItem: ClipFuncItem, pos: Int) {
        ibReplay.visibility = View.GONE
        ibPlay.visibility = View.GONE
        tvTitle.text = item.title
        tvFavorite.text = item.favoriteCount.toString()
        tvLike.text = item.likeCount.toString()
        tvComment.text = item.commentCount.toString()

        Glide.with(ivCover.context)
            .load(item.cover).placeholder(R.drawable.img_nopic_03).into(ivCover)

        val likeRes = takeIf { item.like == true }?.let { R.drawable.ico_nice_forvideo_s }
            ?: let { R.drawable.ico_nice_forvideo }
        tvLike.setCompoundDrawablesRelativeWithIntrinsicBounds(0, likeRes, 0, 0)

        val favoriteRes = takeIf { item.like == true }?.let { R.drawable.btn_favorite_forvideo_s }
            ?: let { R.drawable.btn_favorite_forvideo_n }
        tvFavorite.setCompoundDrawablesRelativeWithIntrinsicBounds(0, favoriteRes, 0, 0)
    }

    fun onUpdateByDeducted(item: VideoItem, clipFuncItem: ClipFuncItem, pos: Int){
//        if (item.deducted) {
//            tvLike.setOnClickListener {
//                clipFuncItem.onLikeClick(item, pos, item.like != true)
//            }
//            tvFavorite.setOnClickListener {
//                clipFuncItem.onFavoriteClick(
//                    item,
//                    pos,
//                    item.favorite != true
//                )
//            }
//            tvComment.setOnClickListener { clipFuncItem.onCommentClick(item) }
//
//            reminder.visibility = View.GONE
//        } else {
//            reminder_btn_vip.setOnClickListener {
//                clipFuncItem.onVipClick()
//            }
//            reminder_btn_promote.setOnClickListener {
//                clipFuncItem.onPromoteClick()
//            }
//            reminder.visibility = View.VISIBLE
//            progress.visibility = View.GONE
//        }
    }
}