package com.dabenxiang.mimi.view.picturedetail

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.CommentViewType
import com.dabenxiang.mimi.view.picturedetail.viewholder.CommentContentViewHolder
import com.dabenxiang.mimi.view.picturedetail.viewholder.CommentTitleViewHolder
import com.dabenxiang.mimi.view.picturedetail.viewholder.PictureDetailViewHolder
import com.dabenxiang.mimi.view.player.CommentAdapter
import com.dabenxiang.mimi.view.player.RootCommentNode
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import timber.log.Timber
import java.util.*

class PictureDetailAdapter(
    val context: Context,
    private val memberPostItem: MemberPostItem,
    private val onPictureDetailListener: OnPictureDetailListener,
    private val onItemClickListener: PhotoGridAdapter.OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_PICTURE_DETAIL = 0
        const val VIEW_TYPE_COMMENT_TITLE = 1
        const val VIEW_TYPE_COMMENT_DATA = 2
    }

    var photoGridAdapter: PhotoGridAdapter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_PICTURE_DETAIL -> {
                Timber.d("@@VIEW_TYPE_PICTURE_DETAIL.....")
                val mView = LayoutInflater.from(context)
                    .inflate(R.layout.item_picture_detail, parent, false)
                PictureDetailViewHolder(mView)
            }
            VIEW_TYPE_COMMENT_TITLE -> {
                Timber.d("@@VIEW_TYPE_COMMENT_TITLE.....")
                val mView = LayoutInflater.from(context)
                    .inflate(R.layout.item_comment_title, parent, false)
                CommentTitleViewHolder(mView)
            }
            else -> {
                Timber.d("@@ELSE.....")
                val mView = LayoutInflater.from(context)
                    .inflate(R.layout.item_comment_content, parent, false)
                CommentContentViewHolder(mView)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_PICTURE_DETAIL
            1 -> VIEW_TYPE_COMMENT_TITLE
            else -> VIEW_TYPE_COMMENT_DATA
        }
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PictureDetailViewHolder -> {
                Timber.d("@@PictureDetailViewHolder.....")
                val contentItem = Gson().fromJson(memberPostItem.content, ContentItem::class.java)

                holder.posterName.text = memberPostItem.postFriendlyName
                holder.posterTime.text =
                    GeneralUtils.getTimeDiff(memberPostItem.creationDate, Date())
                holder.title.text = memberPostItem.title

                val bitmap = LruCacheUtils.getLruCache(memberPostItem.avatarAttachmentId.toString())
                Glide.with(context)
                    .load(bitmap)
                    .circleCrop()
                    .into(holder.avatarImg)

                val isFollow = memberPostItem.isFollow
                if (isFollow) {
                    holder.follow.text = context.getString(R.string.followed)
                    holder.follow.background =
                        context.getDrawable(R.drawable.bg_white_1_stroke_radius_16)
                    holder.follow.setTextColor(context.getColor(R.color.color_white_1))
                } else {
                    holder.follow.text = context.getString(R.string.follow)
                    holder.follow.background =
                        context.getDrawable(R.drawable.bg_red_1_stroke_radius_16)
                    holder.follow.setTextColor(context.getColor(R.color.color_red_1))
                }

                holder.photoGrid.layoutManager = when (contentItem.images.size) {
                    1 -> GridLayoutManager(context, 1)
                    2 -> GridLayoutManager(context, 2)
                    else -> GridLayoutManager(context, 3)
                }
                photoGridAdapter = PhotoGridAdapter(
                    context,
                    contentItem.images,
                    onPictureDetailListener,
                    onItemClickListener
                )
                holder.photoGrid.adapter = photoGridAdapter

                holder.tagChipGroup.removeAllViews()
                memberPostItem.tags.forEach {
                    val chip = LayoutInflater.from(holder.tagChipGroup.context)
                        .inflate(R.layout.chip_item, holder.tagChipGroup, false) as Chip
                    chip.text = it
                    chip.setTextColor(context.getColor(R.color.color_white_1_50))
                    chip.chipBackgroundColor = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.adult_color_status_bar)
                    )
                    holder.tagChipGroup.addView(chip)
                }

                holder.follow.setOnClickListener {
                    onPictureDetailListener.onFollowClick(memberPostItem, position, !isFollow)
                }

            }
            is CommentTitleViewHolder -> {
                Timber.d("@@CommentTitleViewHolder.....")
                holder.newestComment.setOnClickListener {

                }

                holder.topComment.setOnClickListener {

                }

            }
            is CommentContentViewHolder -> {
                Timber.d("@@CommentContentViewHolder.....")
                val commentAdapter = CommentAdapter(true, playerInfoListener, CommentViewType.VIDEO)
                holder.commentRecycler.adapter = commentAdapter
                onPictureDetailListener.onGetCommandInfo(commentAdapter)
            }
        }
    }

    fun updatePhotoGridItem(position: Int) {
        photoGridAdapter?.notifyItemChanged(position)
    }

    private val playerInfoListener = object : CommentAdapter.PlayerInfoListener {
        override fun sendComment(replyId: Long?, replyName: String?) {

        }

        override fun expandReply(parentNode: RootCommentNode, succeededBlock: () -> Unit) {

        }

        override fun replyComment(replyId: Long?, replyName: String?) {

        }

        override fun setCommentLikeType(
            replyId: Long?,
            isLike: Boolean,
            succeededBlock: () -> Unit
        ) {

        }

        override fun removeCommentLikeType(replyId: Long?, succeededBlock: () -> Unit) {

        }

        override fun getBitmap(id: Long, succeededBlock: (Bitmap) -> Unit) {

        }
    }

    interface OnPictureDetailListener {
        fun onGetAttachment(id: String, position: Int)
        fun onFollowClick(item: MemberPostItem, position: Int, isFollow: Boolean)
        fun onGetCommandInfo(adapter: CommentAdapter)
    }

}