package com.dabenxiang.mimi.view.textdetail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.OnItemClickListener
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.MembersPostCommentItem
import com.dabenxiang.mimi.model.api.vo.TextContentItem
import com.dabenxiang.mimi.model.enums.CommentType
import com.dabenxiang.mimi.model.enums.CommentViewType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.view.adapter.viewHolder.AdHolder
import com.dabenxiang.mimi.view.picturedetail.PictureDetailAdapter
import com.dabenxiang.mimi.view.picturedetail.viewholder.CommentContentViewHolder
import com.dabenxiang.mimi.view.picturedetail.viewholder.CommentTitleViewHolder
import com.dabenxiang.mimi.view.player.CommentAdapter
import com.dabenxiang.mimi.view.player.CommentLoadMoreView
import com.dabenxiang.mimi.view.player.RootCommentNode
import com.dabenxiang.mimi.view.textdetail.viewholder.TextDetailViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.util.*

class TextDetailAdapter(
    val context: Context,
    private val memberPostItem: MemberPostItem,
    private val onTextDetailListener: OnTextDetailListener,
    private val onItemClickListener: OnItemClickListener,
    private var mAdItem: AdItem? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), KoinComponent {

    companion object {
        const val VIEW_TYPE_TEXT_DETAIL = 0
        const val VIEW_TYPE_COMMENT_TITLE = 1
        const val VIEW_TYPE_COMMENT_DATA = 2
        const val VIEW_TYPE_AD = 3
    }

    private val accountManager: AccountManager by inject()

    private var commentAdapter: CommentAdapter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val mView: View

        val holder = when (viewType) {
            VIEW_TYPE_AD -> {
                mView = LayoutInflater.from(context)
                    .inflate(R.layout.item_ad, parent, false)
                AdHolder(mView)
            }
            VIEW_TYPE_TEXT_DETAIL -> {
                mView = LayoutInflater.from(context)
                    .inflate(R.layout.item_text_detail, parent, false)
                TextDetailViewHolder(mView)
            }
            VIEW_TYPE_COMMENT_TITLE -> {
                mView = LayoutInflater.from(context)
                    .inflate(R.layout.item_comment_title, parent, false)
                CommentTitleViewHolder(mView)
            }
            else -> {
                mView = LayoutInflater.from(context)
                    .inflate(R.layout.item_comment_content, parent, false)
                CommentContentViewHolder(mView)
            }
        }

        mView.setOnClickListener {
            onItemClickListener.onItemClick()
        }

        return holder
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> PictureDetailAdapter.VIEW_TYPE_AD
            1 -> VIEW_TYPE_TEXT_DETAIL
            2 -> VIEW_TYPE_COMMENT_TITLE
            else -> VIEW_TYPE_COMMENT_DATA
        }
    }

    override fun getItemCount(): Int {
        return 4
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AdHolder -> {
                mAdItem?.also { item ->
                    Glide.with(context).load(item.href).into(holder.adImg)
                    holder.adImg.setOnClickListener {
                        onTextDetailListener.onOpenWebView(item.target)
                    }
                }
            }
            is TextDetailViewHolder -> {
                val contentItem = try {
                    Gson().fromJson(memberPostItem.content, TextContentItem::class.java)
                } catch (e: Exception) {
                    Timber.e(e)
                    TextContentItem()
                }

                holder.posterName.text = memberPostItem.postFriendlyName
                holder.posterTime.text = GeneralUtils.getTimeDiff(
                    memberPostItem.creationDate, Date()
                )
                holder.title.text = memberPostItem.title
                holder.desc.text = contentItem.text

                onTextDetailListener.onGetAttachment(
                    memberPostItem.avatarAttachmentId,
                    holder.avatarImg
                )

                if (accountManager.getProfile().userId != memberPostItem.creatorId) {
                    holder.follow.visibility = View.VISIBLE
                    val isFollow = memberPostItem.isFollow
                    if (isFollow) {
                        holder.follow.text = context.getString(R.string.followed)
                        holder.follow.background =
                            context.getDrawable(R.drawable.bg_white_1_stroke_radius_16)
                        holder.follow.setTextColor(context.getColor(R.color.color_black_1_60))
                    } else {
                        holder.follow.text = context.getString(R.string.follow)
                        holder.follow.background =
                            context.getDrawable(R.drawable.bg_red_1_stroke_radius_16)
                        holder.follow.setTextColor(context.getColor(R.color.color_red_1))
                    }
                    holder.follow.setOnClickListener {
                        onTextDetailListener.onFollowClick(memberPostItem, position, !isFollow)
                    }
                } else {
                    holder.follow.visibility = View.GONE
                }

                holder.tagChipGroup.removeAllViews()
                memberPostItem.tags?.forEach {
                    val chip = LayoutInflater.from(holder.tagChipGroup.context)
                        .inflate(R.layout.chip_item, holder.tagChipGroup, false) as Chip
                    chip.text = it
                    chip.setTextColor(context.getColor(R.color.color_black_1_50))
                    chip.setOnClickListener { view ->
                        onTextDetailListener.onChipClick(
                            PostType.TEXT,
                            (view as Chip).text.toString()
                        )
                    }
                    holder.tagChipGroup.addView(chip)
                }

                holder.avatarImg.setOnClickListener {
                    onTextDetailListener.onAvatarClick(
                        memberPostItem.creatorId,
                        memberPostItem.postFriendlyName
                    )
                }
            }
            is CommentTitleViewHolder -> {
                holder.newestComment.setOnClickListener {
                    holder.newestComment.setTextColor(context.getColor(R.color.color_red_1))
                    holder.topComment.setTextColor(context.getColor(R.color.color_black_1_30))
                    updateCommandItem(CommentType.NEWEST)
                }

                holder.topComment.setOnClickListener {
                    holder.topComment.setTextColor(context.getColor(R.color.color_red_1))
                    holder.newestComment.setTextColor(context.getColor(R.color.color_black_1_30))
                    updateCommandItem(CommentType.TOP)
                }
            }
            is CommentContentViewHolder -> {
                holder.noCommentLayout.visibility = if (memberPostItem.commentCount > 0) {
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }

                commentAdapter = CommentAdapter(
                    true,
                    playerInfoListener,
                    CommentViewType.COMMON
                ).apply {
                    loadMoreModule.apply {
                        isEnableLoadMore = true
                        isAutoLoadMore = true
                        isEnableLoadMoreIfNotFullPage = false
                        loadMoreView = CommentLoadMoreView(true, CommentViewType.COMMON)
                    }
                }

                holder.commentRecycler.adapter = commentAdapter
                updateCommandItem(CommentType.NEWEST)
            }
        }
    }

    fun updateContent(item: MemberPostItem) {
        memberPostItem.content = item.content
        notifyItemChanged(VIEW_TYPE_TEXT_DETAIL)
    }

    fun setupAdItem(item: AdItem) {
        mAdItem = item
    }

    private fun updateCommandItem(type: CommentType) {
        onTextDetailListener.onGetCommandInfo(commentAdapter!!, type)
    }

    private val playerInfoListener = object : CommentAdapter.PlayerInfoListener {
        override fun sendComment(replyId: Long?, replyName: String?) {
            onTextDetailListener.onReplyComment(replyId, replyName)
        }

        override fun expandReply(parentNode: RootCommentNode, succeededBlock: () -> Unit) {
            onTextDetailListener.onGetReplyCommand(parentNode, succeededBlock)
        }

        override fun replyComment(replyId: Long?, replyName: String?) {
            onTextDetailListener.onReplyComment(replyId, replyName)
        }

        override fun setCommentLikeType(
            replyId: Long?,
            isLike: Boolean,
            succeededBlock: () -> Unit
        ) {
            onTextDetailListener.onCommandLike(replyId, isLike, succeededBlock)
        }

        override fun removeCommentLikeType(replyId: Long?, succeededBlock: () -> Unit) {
            onTextDetailListener.onCommandDislike(replyId, succeededBlock)
        }

        override fun onMoreClick(item: MembersPostCommentItem) {
            onTextDetailListener.onMoreClick(item)
        }

        override fun onAvatarClick(userId: Long, name: String) {
            onTextDetailListener.onAvatarClick(userId, name)
        }

        override fun loadAvatar(id: Long?, view: ImageView) {
            onTextDetailListener.onGetAttachment(id, view)
        }
    }

    interface OnTextDetailListener {
        fun onGetAttachment(id: Long?, view: ImageView)
        fun onFollowClick(item: MemberPostItem, position: Int, isFollow: Boolean)
        fun onGetCommandInfo(adapter: CommentAdapter, type: CommentType)
        fun onGetReplyCommand(parentNode: RootCommentNode, succeededBlock: () -> Unit)
        fun onCommandLike(commentId: Long?, isLike: Boolean, succeededBlock: () -> Unit)
        fun onCommandDislike(commentId: Long?, succeededBlock: () -> Unit)
        fun onReplyComment(replyId: Long?, replyName: String?)
        fun onMoreClick(item: MembersPostCommentItem)
        fun onChipClick(type: PostType, tag: String)
        fun onOpenWebView(url: String)
        fun onAvatarClick(userId: Long, name: String)
    }
}