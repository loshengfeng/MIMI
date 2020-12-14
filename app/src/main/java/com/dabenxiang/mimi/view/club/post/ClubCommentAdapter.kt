package com.dabenxiang.mimi.view.club.post

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
import com.dabenxiang.mimi.model.enums.CommentType
import com.dabenxiang.mimi.model.enums.CommentViewType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.adapter.viewHolder.AdHolder
import com.dabenxiang.mimi.view.player.CommentAdapter
import com.dabenxiang.mimi.view.player.CommentLoadMoreView
import com.dabenxiang.mimi.view.player.RootCommentNode
import org.koin.core.component.KoinComponent

class ClubCommentAdapter(
    val context: Context,
    private val memberPostItem: MemberPostItem,
    private val onTextDetailListener: OnTextDetailListener,
    private val onItemClickListener: OnItemClickListener,
    private var mAdItem: AdItem? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), KoinComponent {

    companion object {
        const val VIEW_TYPE_COMMENT_TITLE = 1
        const val VIEW_TYPE_COMMENT_DATA = 2
        const val VIEW_TYPE_AD = 3
    }

    private var commentAdapter: CommentAdapter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val mView: View

        val holder = when (viewType) {
            VIEW_TYPE_AD -> {
                mView = LayoutInflater.from(context)
                    .inflate(R.layout.item_ad, parent, false)
                AdHolder(mView)
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
            0 -> VIEW_TYPE_AD
            1 -> VIEW_TYPE_COMMENT_TITLE
            else -> VIEW_TYPE_COMMENT_DATA
        }
    }

    override fun getItemCount(): Int {
        return 3
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
                    playerInfoListener,
                    CommentViewType.COMMON
                ).apply {
                    loadMoreModule.apply {
                        isEnableLoadMore = true
                        isAutoLoadMore = true
                        isEnableLoadMoreIfNotFullPage = false
                        loadMoreView = CommentLoadMoreView(CommentViewType.COMMON)
                    }
                }

                holder.commentRecycler.adapter = commentAdapter
                updateCommandItem(CommentType.NEWEST)
            }
        }
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