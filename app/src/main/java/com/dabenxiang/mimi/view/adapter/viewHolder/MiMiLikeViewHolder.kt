package com.dabenxiang.mimi.view.adapter.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.dabenxiang.mimi.callback.MyLikeListener
import com.dabenxiang.mimi.model.api.vo.PostFavoriteItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.item_clip_post.view.*
import kotlinx.android.synthetic.main.item_follow_member.view.iv_photo
import kotlinx.android.synthetic.main.item_follow_member.view.tv_follow
import kotlinx.android.synthetic.main.item_follow_member.view.tv_name
import org.koin.core.component.KoinComponent
import timber.log.Timber

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
        myPostListener: MyLikeListener,
        searchTag: String = ""
    ) {
        Timber.d("YYYY , item = ${item}")
    }
//
//    override fun updated(position: Int) {
////        data?.avatarAttachmentId?.let { id -> listener.onGetAttachment(id, ivPhoto) }
////        tvName.text = data?.posterName
////        tvTime.text = data?.postDate
////        clFollow.setOnClickListener {
////            data?.userId?.let { userId ->
////                listener.onCancelFollow(
////                    userId,
////                    position
////                )
////            }
////        }
//
//        clClipPost.setBackgroundColor(App.self.getColor(R.color.color_white_1))
//        tvName.setTextColor(App.self.getColor(R.color.color_black_1))
//        tvTime.setTextColor(App.self.getColor(R.color.color_black_1_50))
//        tvTitle.setTextColor(App.self.getColor(R.color.color_black_1))
//        tvLikeCount.setTextColor(App.self.getColor(R.color.color_black_1))
//        tvFavoriteCount.setTextColor(App.self.getColor(R.color.color_black_1))
//        tvCommentCount.setTextColor(App.self.getColor(R.color.color_black_1))
//        ivComment.setImageResource(R.drawable.ico_messege_adult_gray)
//        ivMore.setImageResource(R.drawable.btn_more_gray_n)
//        vSeparator.setBackgroundColor(App.self.getColor(R.color.color_black_1_05))
//
////        tvName.text = data?.friendlyName
////        tvTime.text = GeneralUtils.getTimeDiff(data?.creationDate, Date())
////        tvTitle.text = item.title
////        tvFollow.visibility =
////            if (accountManager.getProfile().userId == item.creatorId) View.GONE else View.VISIBLE
////
////        attachmentListener.onGetAttachment(item.avatarAttachmentId, ivAvatar, LoadImageType.AVATAR)
////        ivAvatar.setOnClickListener {
////            myPostListener.onAvatarClick(item.creatorId,item.postFriendlyName)
////        }
////
////        tagChipGroup.removeAllViews()
////        item.tags?.forEach {
////            val chip = LayoutInflater.from(tagChipGroup.context)
////                .inflate(R.layout.chip_item, tagChipGroup, false) as Chip
////            chip.text = it
////            chip.setTextColor(tagChipGroup.context.getColor(R.color.color_black_1_50))
////            chip.chipBackgroundColor = ColorStateList.valueOf(
////                ContextCompat.getColor(tagChipGroup.context, R.color.color_black_1_05)
////            )
////            chip.setOnClickListener { view ->
////                myPostListener.onChipClick(PostType.VIDEO, (view as Chip).text.toString())
////            }
////            tagChipGroup.addView(chip)
////        }
////
////        val contentItem = Gson().fromJson(item.content, MediaContentItem::class.java)
////
////        tvLength.text = contentItem.shortVideo?.length
////        contentItem.images?.also { images ->
////            Timber.i("images $images")
////            if (!TextUtils.isEmpty(images[0].url)) {
////                Glide.with(ivPhoto.context)
////                    .load(images[0].url).placeholder(R.drawable.img_nopic_03).into(ivPhoto)
////            } else {
////                attachmentListener.onGetAttachment(
////                    images[0].id.toLongOrNull(),
////                    ivPhoto,
////                    LoadImageType.PICTURE_THUMBNAIL
////                )
////            }
////        }
//
////        if (isMe) {
////            tvFollow.visibility = View.GONE
////        } else {
////            tvFollow.visibility = View.VISIBLE
////            updateFollow(item)
////            tvFollow.setOnClickListener {
////                itemList?.also { myPostListener.onFollowClick(it, position, !item.isFollow) }
////            }
////        }
//
//        tvFollow.visibility =  View.GONE
//
//        ivMore.setOnClickListener {
////            myPostListener.onMoreClick(item, position)
//        }
//
////        updateFavorite(item)
////        val onFavoriteClickListener = View.OnClickListener {
////            item.isFavorite = !item.isFavorite
////            item.favoriteCount =
////                if (item.isFavorite) item.favoriteCount + 1 else item.favoriteCount - 1
////            myPostListener.onFavoriteClick(
////                item,
////                position,
////                item.isFavorite,
////                AttachmentType.ADULT_HOME_CLIP
////            )
////        }
////        ivFavorite.setOnClickListener(onFavoriteClickListener)
////        tvFavoriteCount.setOnClickListener(onFavoriteClickListener)
////
////        updateLike(item)
////        val onLikeClickListener = View.OnClickListener {
////            item.likeType = if (item.likeType == LikeType.LIKE) LikeType.DISLIKE else LikeType.LIKE
////            item.likeCount =
////                if (item.likeType == LikeType.LIKE) item.likeCount + 1 else item.likeCount - 1
////            myPostListener.onLikeClick(item, position, item.likeType == LikeType.LIKE)
////        }
////        ivLike.setOnClickListener(onLikeClickListener)
////        tvLikeCount.setOnClickListener(onLikeClickListener)
////
////        tvCommentCount.text = item.commentCount.toString()
////        val onCommentClickListener = View.OnClickListener {
////            itemList?.also { myPostListener.onClipCommentClick(it, position) }
////        }
////        ivComment.setOnClickListener(onCommentClickListener)
////        tvCommentCount.setOnClickListener(onCommentClickListener)
//    }
//
//    override fun updated() {
//    }
}