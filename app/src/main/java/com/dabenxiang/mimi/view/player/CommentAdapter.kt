package com.dabenxiang.mimi.view.player

import android.graphics.Bitmap
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.extension.setBtnSolidColor
import com.dabenxiang.mimi.model.api.vo.MembersPostCommentItem
import com.dabenxiang.mimi.model.enums.CommentViewType
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.yulichswift.roundedview.widget.RoundedTextView
import java.text.SimpleDateFormat
import java.util.*

class CommentAdapter(isAdult: Boolean, listener: PlayerInfoListener, type: CommentViewType) :
    BaseNodeAdapter(), LoadMoreModule {

    companion object {
        const val EXPAND_COLLAPSE_PAYLOAD = 99
    }

    interface PlayerInfoListener {
        fun sendComment(replyId: Long?, replyName: String?)
        fun expandReply(parentNode: RootCommentNode, succeededBlock: () -> Unit)
        fun replyComment(replyId: Long?, replyName: String?)
        fun setCommentLikeType(replyId: Long?, isLike: Boolean, succeededBlock: () -> Unit)
        fun removeCommentLikeType(replyId: Long?, succeededBlock: () -> Unit)
        fun getBitmap(id: Long, succeededBlock: (Bitmap) -> Unit)
        fun onMoreClick(item: MembersPostCommentItem)
    }

    init {
        addNodeProvider(RootCommentProvider(isAdult, listener, type))
        addNodeProvider(NestedCommentProvider(isAdult, listener, type))
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return when (data[position]) {
            is RootCommentNode -> 1
            is NestedCommentNode -> 2
            else -> 0
        }
    }
}

class RootCommentProvider(
    private val isAdult: Boolean,
    private val listener: CommentAdapter.PlayerInfoListener,
    private val type: CommentViewType
) : BaseCommentProvider(isAdult, listener, type) {

    override val itemViewType: Int
        get() = 1

    override val layoutId: Int
        get() = R.layout.item_comment_root

    init {
        addChildClickViewIds(
            R.id.tv_like,
            R.id.tv_unlike,
            R.id.btn_reply,
            R.id.btn_show_comment_reply,
            R.id.btn_more
        )
    }

    override fun convert(holder: BaseViewHolder, item: BaseNode) {
        val node = item as RootCommentNode
        dataConvert(holder, node.data)

        holder.getView<RoundedTextView>(R.id.btn_show_comment_reply).also {
            if (!node.isExpanded && node.data.commentCount != null && node.data.commentCount > 0) {

                val solidColor = when (type) {
                    CommentViewType.CLIP -> R.color.transparent
                    CommentViewType.VIDEO -> {
                        if (isAdult) R.color.color_white_1_10
                        else R.color.color_black_1_05
                    }
                    else -> R.color.color_white_1_10
                }.let { colorRes ->
                    it.resources.getColor(colorRes, null)
                }

                val pressedColor = when (type) {
                    CommentViewType.CLIP -> R.color.transparent
                    CommentViewType.VIDEO -> {
                        if (isAdult) R.color.color_white_1_30
                        else R.color.color_black_1_30
                    }
                    else -> R.color.color_white_1_30
                }.let { colorRes ->
                    it.resources.getColor(colorRes, null)
                }

                it.setBtnSolidColor(
                    solidColor,
                    solidColor,
                    it.resources.getDimension(R.dimen.dp_10)
                )
                it.text = String.format(
                    it.resources.getString(R.string.n_reply),
                    node.data.commentCount
                )

                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }
    }

    override fun convert(holder: BaseViewHolder, data: BaseNode, payloads: List<Any>) {
//        Timber.d("Payloads: $payloads, Data: $data")
//        (data as RootCommentNode).also {
//            Timber.d("IsExpanded: ${it.isExpanded}")
//        }
    }

    override fun onChildClick(holder: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        val actualData = data as RootCommentNode
        when (view.id) {
            R.id.btn_show_comment_reply -> {
                if (actualData.isExpanded) {
                    getAdapter()?.expandOrCollapse(
                        position = position,
                        animate = false,
                        notify = true,
                        parentPayload = CommentAdapter.EXPAND_COLLAPSE_PAYLOAD
                    )
                } else {
                    val adapter = getAdapter() as? CommentAdapter
                    if (adapter != null) {
                        listener.expandReply(actualData) {
                            adapter.expand(
                                position = position,
                                animate = false,
                                notify = true,
                                parentPayload = CommentAdapter.EXPAND_COLLAPSE_PAYLOAD
                            )
                        }
                    }
                }
            }
            R.id.tv_like -> {
                val isLike = true
                if (isRemoveLikeData(actualData.data, isLike)) {
                    listener.removeCommentLikeType(actualData.data.id) {
                        removeLikeData(actualData.data, isLike)
                        updateLikeCountAndDislikeCount(holder, actualData.data)
                    }
                } else {
                    listener.setCommentLikeType(actualData.data.id, isLike) {
                        setLikeData(actualData.data, isLike)
                        updateLikeCountAndDislikeCount(holder, actualData.data)
                    }
                }
            }
            R.id.tv_unlike -> {
                val isLike = false
                if (isRemoveLikeData(actualData.data, isLike)) {
                    listener.removeCommentLikeType(actualData.data.id) {
                        removeLikeData(actualData.data, isLike)
                        updateLikeCountAndDislikeCount(holder, actualData.data)
                    }
                } else {
                    listener.setCommentLikeType(actualData.data.id, isLike) {
                        setLikeData(actualData.data, isLike)
                        updateLikeCountAndDislikeCount(holder, actualData.data)
                    }
                }
            }
            R.id.btn_reply -> {
                listener.sendComment(actualData.data.id, actualData.data.postName)
            }
            R.id.btn_more -> {
                listener.onMoreClick(actualData.data)
            }
        }
    }
}

class NestedCommentProvider(
    isAdult: Boolean,
    val listener: CommentAdapter.PlayerInfoListener,
    type: CommentViewType
) : BaseCommentProvider(isAdult, listener, type) {
    override val itemViewType: Int
        get() = 2

    override val layoutId: Int
        get() = R.layout.item_comment_nested

    init {
        addChildClickViewIds(R.id.tv_like, R.id.tv_unlike, R.id.btn_reply, R.id.btn_more)
    }

    override fun convert(holder: BaseViewHolder, item: BaseNode) {
        dataConvert(holder, (item as NestedCommentNode).data)
    }

    override fun onChildClick(holder: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        val actualData = data as NestedCommentNode
        when (view.id) {
            R.id.tv_like -> {
                val isLike = true
                if (isRemoveLikeData(actualData.data, isLike)) {
                    listener.removeCommentLikeType(actualData.data.id) {
                        removeLikeData(actualData.data, isLike)
                        updateLikeCountAndDislikeCount(holder, actualData.data)
                    }
                } else {
                    listener.setCommentLikeType(actualData.data.id, isLike) {
                        setLikeData(actualData.data, isLike)
                        updateLikeCountAndDislikeCount(holder, actualData.data)
                    }
                }
                updateLikeCountAndDislikeCount(holder, actualData.data)
            }
            R.id.tv_unlike -> {
                val isLike = false
                if (isRemoveLikeData(actualData.data, isLike)) {
                    listener.removeCommentLikeType(actualData.data.id) {
                        removeLikeData(actualData.data, isLike)
                        updateLikeCountAndDislikeCount(holder, actualData.data)
                    }
                } else {
                    listener.setCommentLikeType(actualData.data.id, isLike) {
                        setLikeData(actualData.data, isLike)
                        updateLikeCountAndDislikeCount(holder, actualData.data)
                    }
                }
            }
            R.id.btn_reply -> {
                val adapter = getAdapter() as? CommentAdapter
                if (adapter != null) {
                    val parentNode = actualData.parentNodeRef.get()!!
                    val parentPosition = adapter.findParentNode(data)
                    if (parentPosition > -1) {
                        listener.replyComment(parentNode.data.id, actualData.data.postName)
                    }
                }
            }
            R.id.btn_more -> {
                listener.onMoreClick(actualData.data)
            }
        }
    }
}

abstract class BaseCommentProvider(
    private val isAdult: Boolean,
    private val listener: CommentAdapter.PlayerInfoListener,
    private val type: CommentViewType
) : BaseNodeProvider() {

    protected fun dataConvert(holder: BaseViewHolder, data: MembersPostCommentItem) {
        holder.getView<View>(R.id.line_Top).apply {
            when (type) {
                CommentViewType.VIDEO -> {
                    if (holder.layoutPosition == 1) {
                        visibility = View.GONE
                    } else {
                        visibility = View.VISIBLE
                        when {
                            isAdult -> R.color.color_white_1
                            else -> R.color.color_black_1_05
                        }.run {
                            setBackgroundResource(this)
                        }
                    }
                }
                CommentViewType.CLIP -> {
                    if (holder.layoutPosition == 0) {
                        visibility = View.GONE
                    } else {
                        visibility = View.VISIBLE
                        setBackgroundResource(R.color.color_black_1_20)
                    }
                }
                else -> {
                    if (holder.layoutPosition == 0) {
                        visibility = View.GONE
                    } else {
                        visibility = View.VISIBLE
                        setBackgroundResource(R.color.color_black_1_20)
                    }
                }
            }
        }

        holder.getView<ImageView>(R.id.iv_avatar).apply {
            data.postAvatarAttachmentId.toString().takeIf { !TextUtils.isEmpty(it) }?.also { id ->
                LruCacheUtils.getLruCache(id)?.also { bitmap ->
                    Glide.with(context).load(bitmap).circleCrop().into(this)
                } ?: run {
                    listener.getBitmap(id.toLong()) { bitmap -> updateAvatar(holder, bitmap) }
                }
            }
        }

        holder.setBackgroundResource(
            R.id.layout_root,
            when (type) {
                CommentViewType.CLIP -> R.color.transparent
                CommentViewType.VIDEO -> {
                    if (holder.layoutPosition == 1) {
                        if (isAdult) R.drawable.bg_adult_comment_top_radius_10
                        else R.drawable.bg_comment_top_radius_10
                    } else {
                        if (isAdult) R.color.color_white_1_10
                        else R.color.color_gray_2
                    }
                }
                else -> {
                    if (holder.layoutPosition == 0) R.drawable.bg_adult_comment_top_radius_10
                    else R.color.color_white_1_10
                }
            }
        )

        holder.getView<ImageView>(R.id.iv_avatar)

        holder.setText(R.id.tv_name, data.postName)
        holder.setTextColorRes(R.id.tv_name, getTextColor())
        holder.setTextColorRes(R.id.tv_date, getTextColor())
        holder.setText(R.id.tv_message, data.content)
        holder.setTextColorRes(R.id.tv_message, getMessageTextColor())
        holder.setTextColorRes(R.id.tv_like, getMessageTextColor())
        holder.setTextColorRes(R.id.tv_unlike, getMessageTextColor())

        holder.setTextColorRes(R.id.btn_reply, getTextColor())

        holder.setGone(R.id.btn_show_comment_reply, true)

        data.creationDate?.let { date ->
            SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault()).format(date)
        }.also {
            holder.setText(R.id.tv_date, it)
        }

        holder.getView<ImageView>(R.id.btn_more)
            .setBackgroundResource(if (isAdult) R.drawable.btn_more_white_n else R.drawable.btn_more_gray_n)
        updateLikeCountAndDislikeCount(holder, data)
    }

    fun updateLikeCountAndDislikeCount(holder: BaseViewHolder, data: MembersPostCommentItem) {
        holder.setText(R.id.tv_like, (data.likeCount ?: 0L).toString())
        holder.getView<TextView>(R.id.tv_like).setCompoundDrawablesRelativeWithIntrinsicBounds(
            getLikeRes(data.likeType == 0),
            0,
            0,
            0
        )
        holder.setText(R.id.tv_unlike, (data.dislikeCount ?: 0L).toString())
        holder.getView<TextView>(R.id.tv_unlike).setCompoundDrawablesRelativeWithIntrinsicBounds(
            getDislikeRes(data.likeType == 1),
            0,
            0,
            0
        )
    }

    private fun updateAvatar(holder: BaseViewHolder, bitmap: Bitmap) {
        holder.getView<ImageView>(R.id.iv_avatar).apply {
            Glide.with(context).load(bitmap).circleCrop().into(this)
        }
    }

    private fun getTextColor() = if (isAdult) R.color.color_white_1_50 else R.color.color_black_1_50

    private fun getMessageTextColor() =
        if (isAdult) R.color.color_white_1 else R.color.color_black_1

    private fun getLikeRes(isLike: Boolean): Int {
        return if (isLike) {
            R.drawable.ico_nice_s
        } else {
            if (isAdult) {
                R.drawable.ico_nice
            } else {
                R.drawable.ico_nice_gray
            }
        }
    }

    private fun getDislikeRes(isDislike: Boolean): Int {
        return if (isDislike) {
            R.drawable.ico_bad_s
        } else {
            if (isAdult) {
                R.drawable.ico_bad
            } else {
                R.drawable.ico_bad_gray
            }
        }
    }

    protected fun isRemoveLikeData(data: MembersPostCommentItem, isLike: Boolean): Boolean {
        return when (data.likeType) {
            0 -> isLike
            1 -> !isLike
            else -> false
        }
    }

    protected fun removeLikeData(data: MembersPostCommentItem, isLike: Boolean) {
        data.likeType = null
        if (isLike) {
            data.likeCount = decreaseLikeCount(data.likeCount)
        } else {
            data.dislikeCount = decreaseLikeCount(data.dislikeCount)
        }
    }

    private fun increaseLikeCount(count: Long?): Long {
        return if (count == null) 1L else count + 1
    }

    private fun decreaseLikeCount(count: Long?): Long {
        return if (count == null) 0L else count - 1
    }

    protected fun setLikeData(data: MembersPostCommentItem, isLike: Boolean) {
        when (data.likeType) {
            0 -> {
                if (!isLike) {
                    data.likeCount = decreaseLikeCount(data.likeCount)
                }
            }
            1 -> {
                if (isLike) {
                    data.dislikeCount = decreaseLikeCount(data.dislikeCount)
                }
            }
        }

        if (isLike) {
            data.likeType = 0
            data.likeCount = increaseLikeCount(data.likeCount)
        } else {
            data.likeType = 1
            data.dislikeCount = increaseLikeCount(data.dislikeCount)
        }
    }
}