package com.dabenxiang.mimi.view.adapter.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MyLikeListener
import com.dabenxiang.mimi.model.api.vo.PostFavoriteItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.item_clip_post.view.*
import kotlinx.android.synthetic.main.item_follow_member.view.iv_photo
import kotlinx.android.synthetic.main.item_follow_member.view.tv_follow
import kotlinx.android.synthetic.main.item_follow_member.view.tv_name
import org.koin.core.component.KoinComponent
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class MiMiLikeViewHolder(
    itemView: View
) : BaseViewHolder(itemView), KoinComponent {
    private val clClipPost: ConstraintLayout = itemView.cl_clip_post
    private val ivAvatar: ImageView = itemView.img_avatar
    private val tvName: TextView = itemView.tv_name
    private val tvTime: TextView = itemView.tv_time
    private val tvTitle: TextView = itemView.tv_title
    private val tvFollow: TextView = itemView.tv_follow
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
        item: PostFavoriteItem,
        itemList: List<PostFavoriteItem>?,
        position: Int,
        listener: MyLikeListener,
        searchTag: String = ""
    ) {
        val contex = App.self
        clClipPost.setBackgroundColor(contex.getColor(R.color.color_white_1))
        tvName.setTextColor(contex.getColor(R.color.color_black_1))
        tvTime.setTextColor(contex.getColor(R.color.color_black_1_50))
        tvTitle.setTextColor(contex.getColor(R.color.color_black_1))
        tvLikeCount.setTextColor(contex.getColor(R.color.color_black_1))
        tvFavoriteCount.setTextColor(contex.getColor(R.color.color_black_1))
        tvCommentCount.setTextColor(contex.getColor(R.color.color_black_1))
        ivComment.setImageResource(R.drawable.ico_messege_adult_gray)
        ivMore.setImageResource(R.drawable.btn_more_gray_n)
        vSeparator.setBackgroundColor(App.self.getColor(R.color.color_black_1_05))
        tagChipGroup.removeAllViews()

        item.tags?.forEach {
            val chip = LayoutInflater.from(tagChipGroup.context)
                .inflate(R.layout.chip_item, tagChipGroup, false) as Chip
            chip.text = it
            if (it == searchTag) chip.setTextColor(tagChipGroup.context.getColor(R.color.color_red_1))
            else chip.setTextColor(tagChipGroup.context.getColor(R.color.color_black_1_50))
            chip.setOnClickListener { view ->
                listener.onChipClick(item, (view as Chip).text.toString())
            }
            tagChipGroup.addView(chip)
        }
        tagChipGroup.setOnClickListener { view ->
            //TODO go to search
            Timber.i("MiMiLikeViewHolder tagChipGroup Click  $item")
//            listener.onChipClick(item, (view as Chip).text.toString())

        }
        tvTitle.text = item.title
        tvName.text = item.posterName
        ivAvatar.setOnClickListener { view ->
            Timber.i("MiMiLikeViewHolder ivAvatar Click  $item")
//            listener.onChipClick(item, (view as Chip).text.toString())
            //TODO go to poster
        }
        tvName.setOnClickListener { view ->
//            listener.onChipClick(item, (view as Chip).text.toString())
            //TODO go to poster
        }
        tvTime.text = item.postDate.let { date ->
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm",
                Locale.getDefault()
            ).format(date)
        }
        ivPhoto.setOnClickListener { view ->
            Timber.i("MiMiLikeViewHolder ivPhoto Click  $item")
//            listener.onChipClick(item, (view as Chip).text.toString())
            //TODO go to video dec & play page
        }

        ivLike.setOnClickListener { view ->
            //TODO un-like & re-move item call api
//            listener.onChipClick(item, (view as Chip).text.toString())
        }
        tvLikeCount.setOnClickListener { view ->
            //TODO un-like & re-move item call api
//            listener.onChipClick(item, (view as Chip).text.toString())
        }
//        val contentItem = Gson().fromJson(item.content , MediaContentItem::class.java)
//        tvLength.text = contentItem?.shortVideo?.length
//        contentItem.images?.also {images->
//            if (!TextUtils.isEmpty(images[0].url)) {
//                Glide.with(ivPhoto.context)
//                    .load(images[0].url).placeholder(R.drawable.img_nopic_03).into(ivPhoto)
//            } else {
////                listener.onGetAttachment(
////                    images[0].id.toLongOrNull(),
////                    ivPhoto,
////                    LoadImageType.PICTURE_THUMBNAIL
////                )
//            }
//        }

        ivComment.setOnClickListener { view ->
            //TODO go to Comment
//            listener.onChipClick(item, (view as Chip).text.toString())
        }
        tvCommentCount.setOnClickListener { view ->
            //TODO go to Comment
//            listener.onChipClick(item, (view as Chip).text.toString())
        }

        ivFavorite.setOnClickListener { view ->
            //TODO popup dialog
//            listener.onChipClick(item, (view as Chip).text.toString())
        }
        tvFavoriteCount.setOnClickListener { view ->
            //TODO popup dialog
//            listener.onChipClick(item, (view as Chip).text.toString())
        }


        tvFollow.visibility = View.VISIBLE
        when (item.isFollow) {
            true -> {
                tvFollow.setTextColor(tvFollow.context.getColor(R.color.color_black_1_60))
                tvFollow.setBackgroundResource(R.drawable.bg_gray_6_radius_16)
                tvFollow.setText(R.string.followed)
            }
            else -> {
                tvFollow.setTextColor(tvFollow.context.getColor(R.color.color_red_1))
                tvFollow.setBackgroundResource(R.drawable.bg_red_1_stroke_radius_16)
                tvFollow.setText(R.string.follow)
            }
        }
        tvFollow.setOnClickListener { view ->
            //TODO Follow poster
//            listener.onChipClick(item, (view as Chip).text.toString())
        }
//        setupChipGroup(data?.tags, data?.type)
        tvLikeCount.text = item.likeCount.toString()
//        val res = if (data?.likeType == LikeType.LIKE) {
//            R.drawable.ico_nice_s
//        } else {
//            R.drawable.ico_nice_gray
//        }
//        tvLike.setCompoundDrawablesRelativeWithIntrinsicBounds(res, 0, 0, 0)
        tvFavoriteCount.text = item.favoriteCount.toString()
        tvCommentCount.text = item.commentCount.toString()
    }
}