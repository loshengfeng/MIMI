package com.dabenxiang.mimi.view.adapter.viewHolder

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.callback.OnItemClickListener
import com.dabenxiang.mimi.model.api.vo.MediaContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.adapter.PictureAdapter
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.mypost.MyPostFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_my_post_picture_post.view.*
import kotlinx.android.synthetic.main.item_picture_post.view.chip_group_tag
import kotlinx.android.synthetic.main.item_picture_post.view.img_avatar
import kotlinx.android.synthetic.main.item_picture_post.view.iv_comment
import kotlinx.android.synthetic.main.item_picture_post.view.iv_like
import kotlinx.android.synthetic.main.item_picture_post.view.iv_more
import kotlinx.android.synthetic.main.item_picture_post.view.layout_picture_post_item
import kotlinx.android.synthetic.main.item_picture_post.view.recycler_picture
import kotlinx.android.synthetic.main.item_picture_post.view.tv_comment_count
import kotlinx.android.synthetic.main.item_picture_post.view.tv_like_count
import kotlinx.android.synthetic.main.item_picture_post.view.tv_name
import kotlinx.android.synthetic.main.item_picture_post.view.tv_picture_count
import kotlinx.android.synthetic.main.item_picture_post.view.tv_time
import kotlinx.android.synthetic.main.item_picture_post.view.tv_title
import java.util.*

class MyPostPicturePostHolder(itemView: View) : BaseViewHolder(itemView) {

    val picturePostItemLayout: ConstraintLayout = itemView.layout_picture_post_item
    val avatarImg: ImageView = itemView.img_avatar
    val name: TextView = itemView.tv_name
    val time: TextView = itemView.tv_time
    val title: TextView = itemView.tv_title
    val pictureRecycler: RecyclerView = itemView.recycler_picture
    val pictureCount: TextView = itemView.tv_picture_count
    val tagChipGroup: ChipGroup = itemView.chip_group_tag
    val likeImage: ImageView = itemView.iv_like
    val likeCount: TextView = itemView.tv_like_count
    val commentImage: ImageView = itemView.iv_comment
    val commentCount: TextView = itemView.tv_comment_count
    val moreImage: ImageView = itemView.iv_more
    val tvFavorite: TextView = itemView.tv_favorite_count
    val imgFavorite: ImageView = itemView.tv_favorite

    fun onBind(
        item: MemberPostItem,
        position: Int,
        myPostListener: MyPostFragment.MyPostListener,
        attachmentListener: AttachmentListener
    ) {
        name.text = item.postFriendlyName
        time.text = GeneralUtils.getTimeDiff(item.creationDate, Date())
        title.text = item.title
        updateLikeAndFollowItem(item, position, myPostListener)

        if (LruCacheUtils.getLruCache(item.avatarAttachmentId.toString()) == null) {
            attachmentListener.onGetAttachment(
                item.avatarAttachmentId.toString(),
                position,
                AttachmentType.ADULT_TAB_PICTURE
            )
        } else {
            val bitmap = LruCacheUtils.getLruCache(item.avatarAttachmentId.toString())
            Glide.with(avatarImg.context)
                .load(bitmap)
                .circleCrop()
                .into(avatarImg)
        }

        tagChipGroup.removeAllViews()
        item.tags?.forEach {
            val chip = LayoutInflater.from(tagChipGroup.context)
                .inflate(R.layout.chip_item, tagChipGroup, false) as Chip
            chip.text = it
            chip.setTextColor(tagChipGroup.context.getColor(R.color.color_black_1_50))
            chip.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(tagChipGroup.context, R.color.color_black_1_05)
            )
            chip.setOnClickListener { view ->
                myPostListener.onChipClick(PostType.IMAGE, (view as Chip).text.toString())
            }
            tagChipGroup.addView(chip)
        }

        val contentItem = Gson().fromJson(item.content, MediaContentItem::class.java)
        if (pictureRecycler.adapter == null || pictureCount.tag != position) {
            pictureCount.tag = position
            pictureRecycler.layoutManager = LinearLayoutManager(
                pictureRecycler.context, LinearLayoutManager.HORIZONTAL, false
            )

            pictureRecycler.adapter = PictureAdapter(
                pictureRecycler.context,
                attachmentListener,
                contentItem.images ?: arrayListOf(),
                position,
                object : OnItemClickListener {
                    override fun onItemClick() {
                        item.also { myPostListener.onItemClick(item, AdultTabType.PICTURE) }
                    }
                }
            )
            pictureRecycler.onFlingListener = null
            PagerSnapHelper().attachToRecyclerView(pictureRecycler)

            pictureRecycler.setOnScrollChangeListener { _, _, _, _, _ ->
                val currentPosition =
                    (pictureRecycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                pictureCount.text =
                    "${currentPosition + 1}/${contentItem.images?.size}"
            }

            pictureCount.text = "1/${contentItem.images?.size}"
        }

        commentImage.setOnClickListener {
            item.also { myPostListener.onCommentClick(it, AdultTabType.PICTURE) }
        }

        moreImage.setOnClickListener {
            myPostListener.onMoreClick(item)
        }

        picturePostItemLayout.setOnClickListener {
            item.also { myPostListener.onItemClick(item, AdultTabType.PICTURE) }
        }
    }

    fun updateLikeAndFollowItem(item: MemberPostItem, position: Int, myPostListener: MyPostFragment.MyPostListener) {
        likeCount.text = item.likeCount.toString()
        commentCount.text = item.commentCount.toString()

        val likeType = item.likeType
        val isLike: Boolean
        if (likeType == LikeType.LIKE) {
            isLike = true
            likeImage.setImageResource(R.drawable.ico_nice_s)
        } else {
            isLike = false
            likeImage.setImageResource(R.drawable.ico_nice_gray)
        }

        likeImage.setOnClickListener {
            myPostListener.onLikeClick(item, position, !isLike)
        }

        tvFavorite.text = item.favoriteCount.toString()
        if (item.isFavorite) {
            imgFavorite.setImageResource(R.drawable.btn_favorite_white_s)
        } else {
            imgFavorite.setImageResource(R.drawable.btn_favorite_n)
        }
    }
}