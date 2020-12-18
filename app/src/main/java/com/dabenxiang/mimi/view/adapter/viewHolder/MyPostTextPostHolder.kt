package com.dabenxiang.mimi.view.adapter.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.TextContentItem
import com.dabenxiang.mimi.model.enums.*
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.GeneralUtils.getSpanString
import com.dabenxiang.mimi.widget.utility.LoadImageUtils
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_text_post.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import timber.log.Timber
import java.util.*

class MyPostTextPostHolder(
    itemView: View
) : BaseViewHolder(itemView), KoinComponent {

    private val textPostItemLayout: ConstraintLayout = itemView.layout_text_post_item
    private val imgAvatar: ImageView = itemView.img_avatar
    private val tvName: TextView = itemView.tv_name
    private val tvTime: TextView = itemView.tv_time
    private val tvTitle: TextView = itemView.tv_title
    private val tvTextDesc: TextView = itemView.tv_text_desc
    private val tagChipGroup: ChipGroup = itemView.chip_group_tag
    private val ivLike: ImageView = itemView.iv_like
    private val tvLikeCount: TextView = itemView.tv_like_count
    private val ivComment: ImageView = itemView.iv_comment
    private val tvCommentCount: TextView = itemView.tv_comment_count
    private val ivMore: ImageView = itemView.iv_more
    private val tvFollow: TextView = itemView.tv_follow
    private val vSeparator: View = itemView.v_separator
    private val textLayout: ConstraintLayout = itemView.layout_text
    private val ivFavorite: ImageView = itemView.iv_favorite
    private val tvFavoriteCount: TextView = itemView.tv_favorite_count

    fun onBind(
            item: MemberPostItem,
            position: Int,
            myPostListener: MyPostListener,
            viewModelScope: CoroutineScope,
            searchStr: String = "",
            searchTag: String = ""
    ) {

        tvName.text = item.postFriendlyName
        tvTime.text = GeneralUtils.getTimeDiff(item.creationDate, Date())
        tvTitle.text = if (searchStr.isNotBlank()) getSpanString(tvTitle.context, item.title, searchStr) else item.title

        try {
            val contentItem = Gson().fromJson(item.content, TextContentItem::class.java)
            tvTextDesc.text = contentItem.text
        } catch (e: Exception) {
            Timber.e(e)
        }

        viewModelScope.launch {
            LoadImageUtils.loadImage(item.avatarAttachmentId, imgAvatar, LoadImageType.AVATAR)
        }

        imgAvatar.setOnClickListener {
            myPostListener.onAvatarClick(item.creatorId, item.postFriendlyName)
        }

        tagChipGroup.removeAllViews()
        item.tags?.forEach {
            val chip = LayoutInflater.from(tagChipGroup.context)
                .inflate(R.layout.chip_item, tagChipGroup, false) as Chip
            chip.text = it
            if (it == searchTag) chip.setTextColor(tagChipGroup.context.getColor(R.color.color_red_1))
            else chip.setTextColor(tagChipGroup.context.getColor(R.color.color_black_1_50))
            chip.setOnClickListener { view ->
                myPostListener.onChipClick(PostType.TEXT, (view as Chip).text.toString())
            }
            tagChipGroup.addView(chip)
        }
        tvFollow.visibility = View.GONE

        updateFavorite(item)
        val onFavoriteClickListener = View.OnClickListener {
            item.isFavorite = !item.isFavorite
            item.favoriteCount =
                if (item.isFavorite) item.favoriteCount + 1 else item.favoriteCount - 1
            updateFavorite(item)
            myPostListener.onFavoriteClick(
                item,
                position,
                item.isFavorite,
                AttachmentType.ADULT_HOME_CLIP
            )
        }
        ivFavorite.setOnClickListener(onFavoriteClickListener)
        tvFavoriteCount.setOnClickListener(onFavoriteClickListener)

        ivMore.setOnClickListener {
            myPostListener.onMoreClick(item, position)
        }

        updateLike(item)
        val onLikeClickListener = View.OnClickListener {
            item.likeType = if (item.likeType == LikeType.LIKE) null else LikeType.LIKE
            item.likeCount =
                if (item.likeType == LikeType.LIKE) item.likeCount + 1 else item.likeCount - 1
            updateLike(item)
            myPostListener.onLikeClick(item, position, item.likeType == LikeType.LIKE)
        }
        ivLike.setOnClickListener(onLikeClickListener)
        tvLikeCount.setOnClickListener(onLikeClickListener)

        tvCommentCount.text = item.commentCount.toString()
        val onCommentClickListener = View.OnClickListener {
            myPostListener.onCommentClick(item, AdultTabType.TEXT)
        }
        ivComment.setOnClickListener(onCommentClickListener)
        tvCommentCount.setOnClickListener(onCommentClickListener)

        textPostItemLayout.setOnClickListener {
            myPostListener.onItemClick(item, AdultTabType.TEXT)
        }
    }

    fun updateLike(item: MemberPostItem) {
        tvLikeCount.text = item.likeCount.toString()

        if (item.likeType == LikeType.LIKE) {
            ivLike.setImageResource(R.drawable.ico_nice_s)
        } else {
            ivLike.setImageResource(R.drawable.ico_nice_gray)
        }
    }

    fun updateFollow(item: MemberPostItem) {
        tvFollow.setText(if (item.isFollow) R.string.followed else R.string.follow)
        tvFollow.setBackgroundResource(if (item.isFollow) R.drawable.bg_white_1_stroke_radius_16 else R.drawable.bg_red_1_stroke_radius_16)
        tvFollow.setTextColor(App.self.getColor(if (item.isFollow) R.color.color_black_1_60 else R.color.color_red_1))
    }

    fun updateFavorite(item: MemberPostItem) {
        tvFavoriteCount.text = item.favoriteCount.toString()

        if (item.isFavorite) {
            ivFavorite.setImageResource(R.drawable.btn_favorite_white_s)
        } else {
            ivFavorite.setImageResource(R.drawable.btn_favorite_n)
        }
    }
}