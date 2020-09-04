package com.dabenxiang.mimi.view.clip

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MediaContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.manager.AccountManager
import com.google.android.exoplayer2.ui.PlayerView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_clip.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject

class ClipViewHolder(view: View) : RecyclerView.ViewHolder(view), KoinComponent {

    private val accountManager: AccountManager by inject()

    var playerView: PlayerView = view.player_view
    var ivCover: ImageView = view.iv_cover
    var ivHead: ImageView = view.iv_head
    var clAvatar: ConstraintLayout = view.cl_avatar
    var ivAdd: ImageView = view.iv_close
    var ibReplay: ImageButton = view.ib_replay
    var ibPlay: ImageButton = view.ib_play
    var ibBack: ImageButton = view.ib_back
    var tvTitle: TextView = view.tv_title
    var tvName: TextView = view.tv_name
    var tvFavorite: TextView = view.iv_favorite
    var tvLike: TextView = view.tv_like
    var tvComment: TextView = view.tv_comment
    var progress: ProgressBar = view.progress_video

    fun onBind(item: MemberPostItem, clipFuncItem: ClipFuncItem, pos: Int) {
        ibReplay.visibility = View.GONE
        ibPlay.visibility = View.GONE
        tvTitle.text = item.title
        tvName.text = String.format(
            tvName.context.resources.getString(R.string.clip_username),
            item.postFriendlyName
        )
        tvFavorite.text = item.favoriteCount.toString()
        tvLike.text = item.likeCount.toString()
        tvComment.text = item.commentCount.toString()

        clipFuncItem.getBitmap(item.avatarAttachmentId, ivHead, LoadImageType.AVATAR)

        var contentItem: MediaContentItem? = null
        try {
            contentItem = Gson().fromJson(item.content, MediaContentItem::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        clipFuncItem.getBitmap(
            contentItem?.images?.get(0)?.id?.toLongOrNull(),
            ivCover,
            LoadImageType.PICTURE_THUMBNAIL
        )

        ibBack.setOnClickListener { clipFuncItem.onBackClick() }

        val likeRes = if (item.likeType == LikeType.LIKE) {
            R.drawable.ico_nice_forvideo_s
        } else {
            R.drawable.ico_nice_forvideo
        }
        tvLike.setCompoundDrawablesRelativeWithIntrinsicBounds(0, likeRes, 0, 0)
        val isLike = item.likeType == LikeType.LIKE
        tvLike.setOnClickListener { clipFuncItem.onLikeClick(item, pos, !isLike) }

        val favoriteRes =
            if (item.isFavorite) R.drawable.btn_favorite_forvideo_s else R.drawable.btn_favorite_forvideo_n
        tvFavorite.setCompoundDrawablesRelativeWithIntrinsicBounds(0, favoriteRes, 0, 0)
        tvFavorite.setOnClickListener { clipFuncItem.onFavoriteClick(item, pos, !item.isFavorite) }

        tvComment.setOnClickListener { clipFuncItem.onCommentClick(item) }

        val isMe = accountManager.getProfile().userId == item.creatorId
        ivAdd.visibility = if (item.isFollow || isMe) View.GONE else View.VISIBLE

        if (!isMe) clAvatar.setOnClickListener {
            clipFuncItem.onFollowClick(
                item,
                pos,
                !item.isFollow
            )
        }
    }
}