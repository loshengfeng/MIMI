package com.dabenxiang.mimi.view.adapter.viewHolder

import android.content.res.ColorStateList
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.callback.MyFollowVideoListener
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.vo.MediaContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.enums.*
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_my_follow_video.view.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.*
import com.dabenxiang.mimi.widget.utility.GeneralUtils.getSpanString

class MyFollowVideoViewHolder(
        itemView: View
) : BaseViewHolder(itemView), KoinComponent {

    private val accountManager: AccountManager by inject()

    private val clClipPost: ConstraintLayout = itemView.cl_clip_post
    private val tvTitle: TextView = itemView.tv_title
    private val ivPhoto: ImageView = itemView.iv_photo
    private val tvLength: TextView = itemView.tv_length
    private val tagChipGroup: ChipGroup = itemView.chip_group_tag
    private val ivLike: ImageView = itemView.iv_like
    private val tvLikeCount: TextView = itemView.tv_like_count
    private val ivComment: ImageView = itemView.iv_comment
    private val tvCommentCount: TextView = itemView.tv_comment_count
    private val ivMore: ImageView = itemView.iv_more
    private val ivFavorite: ImageView = itemView.iv_favorite
    private val tvFavoriteCount: TextView = itemView.tv_favorite_count
    private val layoutClip: ConstraintLayout = itemView.layout_clip
    private val vSeparator: View = itemView.v_separator

    fun onBind(
            item: PlayItem,
            itemList: List<MemberPostItem>?,
            position: Int,
            listener: MyFollowVideoListener,
            searchTag: String = ""
    ) {
        Timber.d("neo, item = ${item}")
        clClipPost.setBackgroundColor(App.self.getColor(R.color.color_white_1))
        tvTitle.setTextColor(App.self.getColor(R.color.color_black_1))
        tvLikeCount.setTextColor(App.self.getColor(R.color.color_black_1))
        tvFavoriteCount.setTextColor(App.self.getColor(R.color.color_black_1))
        tvCommentCount.setTextColor(App.self.getColor(R.color.color_black_1))
        ivComment.setImageResource(R.drawable.ico_messege_adult_gray)
        ivMore.setImageResource(R.drawable.btn_more_gray_n)
        vSeparator.setBackgroundColor(App.self.getColor(R.color.color_black_1_05))

        tvTitle.text = item.title

        tagChipGroup.removeAllViews()
        item.tags?.forEach {
            val chip = LayoutInflater.from(tagChipGroup.context)
                    .inflate(R.layout.chip_item, tagChipGroup, false) as Chip
            chip.text = it
            if (it == searchTag) chip.setTextColor(tagChipGroup.context.getColor(R.color.color_red_1))
            else chip.setTextColor(tagChipGroup.context.getColor(R.color.color_black_1_50))
            chip.setOnClickListener { view ->
                listener.onChipClick(PostType.VIDEO, (view as Chip).text.toString())
            }
            tagChipGroup.addView(chip)
        }
        item.cover?.let { images ->
            Glide.with(ivPhoto.context)
                    .load(images).placeholder(R.drawable.img_nopic_03).into(ivPhoto)
        } ?: kotlin.run {
            Glide.with(ivPhoto.context)
                    .load(0).placeholder(R.drawable.img_nopic_03).into(ivPhoto)
        }
        ivMore.setOnClickListener {
            listener.onMoreClick(item, position)
        }

        updateFavorite(item)
        val onFavoriteClickListener = View.OnClickListener {
            item.favorite = item.favorite != true
            item.favoriteCount =
                    if (item.favorite == true) item.favoriteCount ?: 0 + 1 else item.favoriteCount
                            ?: 0 - 1
            listener.onFavoriteClick(
                    item,
                    position,
                    item.favorite ?: false,
                    MyFollowTabItemType.MIMI_VIDEO
            )
        }
        ivFavorite.setOnClickListener(onFavoriteClickListener)
        tvFavoriteCount.setOnClickListener(onFavoriteClickListener)

        updateLike(item)
        val onLikeClickListener = View.OnClickListener {
            item.like = item.like != true
            item.likeCount =
                    if (item.like == true) (item.likeCount ?: 0 + 1) else (item.likeCount ?: 0 - 1)
            listener.onLikeClick(item, position, item.like == true)
        }
        ivLike.setOnClickListener(onLikeClickListener)
        tvLikeCount.setOnClickListener(onLikeClickListener)

        tvCommentCount.text = item.commentCount.toString()
        val onCommentClickListener = View.OnClickListener {
            listener.onCommentClick(item, MyFollowTabItemType.MIMI_VIDEO)
        }

        ivComment.setOnClickListener  (onCommentClickListener)
        tvCommentCount.setOnClickListener(onCommentClickListener)

        layoutClip.setOnClickListener {
            listener.onItemClick(item, MyFollowTabItemType.MIMI_VIDEO)
        }

    }

    fun updateLike(item: PlayItem) {
        Timber.d("neo, updateList = ${item.likeCount.toString()}")
        tvLikeCount.text = item.likeCount.toString()

        if (item.like == true) {
            ivLike.setImageResource(R.drawable.ico_nice_s)
        } else {
            ivLike.setImageResource(R.drawable.ico_nice_gray)
        }
    }

    fun updateFavorite(item: PlayItem) {
        tvFavoriteCount.text = item.favoriteCount.toString()

        if (item.favorite == true) {
            ivFavorite.setImageResource(R.drawable.btn_favorite_white_s)
        } else {
            ivFavorite.setImageResource(R.drawable.btn_favorite_n)
        }
    }

}