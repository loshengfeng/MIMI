package com.dabenxiang.mimi.view.clip

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.InteractiveHistoryItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.android.synthetic.main.item_clip.view.*
import kotlinx.android.synthetic.main.recharge_reminder.view.*
import timber.log.Timber

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
    var tvRetry: TextView = view.tv_retry
    var reminder: View = view.recharge_reminder
    private var btnVip: View = view.btn_vip
    private var btnPromote: View = view.btn_promote

    fun onBind(item: VideoItem, clipFuncItem: ClipFuncItem) {
        reminder.visibility = View.GONE
        ibReplay.visibility = View.GONE
        ibPlay.visibility = View.GONE
        progress.visibility = View.GONE
        tvRetry.visibility = View.GONE
        tvTitle.text = item.title

        updateCount(item)

        tvTitle.isSelected = true

        clipFuncItem.getDecryptSetting(item.source ?: "")?.takeIf { it.isImageDecrypt }
            ?.let { decryptSettingItem ->
                clipFuncItem.decryptCover(item.cover ?: "", decryptSettingItem) {
                    Glide.with(ivCover.context)
                        .load(it).placeholder(R.drawable.img_nopic_03).into(ivCover)
                }
            } ?: run {
            Glide.with(ivCover.context)
                .load(item.cover).placeholder(R.drawable.img_nopic_03).into(ivCover)
        }
    }

    fun updateAfterM3U8(item: VideoItem, clipFuncItem: ClipFuncItem, isOverdue: Boolean) {
        if (isOverdue) {
            btnVip.setOnClickListener { clipFuncItem.onVipClick() }
            btnPromote.setOnClickListener { clipFuncItem.onPromoteClick() }

            tvFavorite.isClickable = false
            tvComment.isClickable = false
            tvMore.isClickable = false

            reminder.visibility = View.VISIBLE
            progress.visibility = View.GONE

        } else {
            item.deducted = !isOverdue
            tvFavorite.setOnClickListener { clipFuncItem.onFavoriteClick(item, !item.favorite, ::updateFavorite) }
            tvComment.setOnClickListener { clipFuncItem.onCommentClick(item) }
            tvMore.setOnClickListener { clipFuncItem.onMoreClick(item) }

            reminder.visibility = View.GONE
        }
    }

    fun updateInteractiveHistory(item: VideoItem, interactiveHistoryItem: InteractiveHistoryItem) {
        item.commentCount = interactiveHistoryItem.commentCount
        item.favoriteCount = interactiveHistoryItem.favoriteCount
        interactiveHistoryItem.isFavorite?.run { item.favorite = this }

        tvFavorite.text = item.favoriteCount.toString()
        tvComment.text = item.commentCount.toString()

        val favoriteRes = takeIf { item.favorite }?.let { R.drawable.btn_favorite_forvideo_s }
            ?: let { R.drawable.btn_favorite_forvideo_n }
        tvFavorite.setCompoundDrawablesRelativeWithIntrinsicBounds(0, favoriteRes, 0, 0)
    }

    fun updateCount(item: VideoItem) {
        LruCacheUtils.getShortVideoCount(item.id)?.run {
            this.favoriteCount?.also { count ->  item.favoriteCount = count.toLong() }
            this.commentCount?.also { count ->  item.commentCount = count.toLong() }
            this.favorite?.also { favorite ->  item.favorite = favorite }
        }
        tvFavorite.text = item.favoriteCount.toString()
        tvComment.text = item.commentCount.toString()

        val favoriteRes = takeIf { item.favorite }?.let { R.drawable.btn_favorite_forvideo_s }
            ?: let { R.drawable.btn_favorite_forvideo_n }
        tvFavorite.setCompoundDrawablesRelativeWithIntrinsicBounds(0, favoriteRes, 0, 0)
    }

    fun updateFavorite(isFavorite: Boolean, favoriteCount: Int) {
        tvFavorite.text = favoriteCount.toString()
        val favoriteRes = takeIf { isFavorite }?.let { R.drawable.btn_favorite_forvideo_s }
            ?: let { R.drawable.btn_favorite_forvideo_n }
        tvFavorite.setCompoundDrawablesRelativeWithIntrinsicBounds(0, favoriteRes, 0, 0)
    }
}