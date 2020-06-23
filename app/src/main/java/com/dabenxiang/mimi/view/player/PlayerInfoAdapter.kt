package com.dabenxiang.mimi.view.player

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.extension.setBtnSolidColor
import com.dabenxiang.mimi.model.api.vo.MembersPostCommentItem
import com.yulichswift.roundedview.widget.RoundedTextView
import java.text.SimpleDateFormat
import java.util.*

class PlayerInfoAdapter(isAdult: Boolean, listener: PlayerInfoListener) : BaseNodeAdapter(), LoadMoreModule {

    companion object {
        const val EXPAND_COLLAPSE_PAYLOAD = 99
    }

    interface PlayerInfoListener {
        fun expandComment(adapter: PlayerInfoAdapter, parentNode: RootCommentNode, succeededBlock: () -> Unit)
    }

    init {
        addNodeProvider(RootCommentProvider(isAdult, listener))
        addNodeProvider(NestedCommentProvider(isAdult, listener))
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return when (data[position]) {
            is RootCommentNode -> 1
            is NestedCommentNode -> 2
            else -> 0
        }
    }
}

class RootCommentProvider(private val isAdult: Boolean, private val listener: PlayerInfoAdapter.PlayerInfoListener) : BaseCommentProvider(isAdult) {

    override val itemViewType: Int
        get() = 1

    override val layoutId: Int
        get() = R.layout.item_comment_root

    init {
        addChildClickViewIds(R.id.tv_like, R.id.tv_unlike, R.id.btn_reply, R.id.btn_show_comment_reply)
    }

    override fun convert(holder: BaseViewHolder, item: BaseNode) {
        val node = item as RootCommentNode
        dataConvert(holder, node.data)

        holder.getView<RoundedTextView>(R.id.btn_show_comment_reply).also {
            if (!node.isExpanded && node.data.commentCount != null && node.data.commentCount > 0) {
                val solidColor =
                    if (isAdult) {
                        R.color.color_white_1_10
                    } else {
                        R.color.color_black_1_05
                    }.let { colorRes ->
                        it.resources.getColor(colorRes, null)
                    }

                val pressedColor =
                    if (isAdult) {
                        R.color.color_white_1_30
                    } else {
                        R.color.color_black_1_30
                    }.let { colorRes ->
                        it.resources.getColor(colorRes, null)
                    }

                it.setBtnSolidColor(solidColor, solidColor, it.resources.getDimension(R.dimen.dp_10))
                it.text = String.format(it.resources.getString(R.string.n_reply), node.data.commentCount)

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
        val realData = data as RootCommentNode
        if (view.id == R.id.btn_show_comment_reply) {
            if (realData.isExpanded) {
                getAdapter()?.expandOrCollapse(
                    position = position,
                    animate = false,
                    notify = true,
                    parentPayload = PlayerInfoAdapter.EXPAND_COLLAPSE_PAYLOAD
                )
            } else {
                val adapter = getAdapter() as? PlayerInfoAdapter
                if (adapter != null) {
                    listener.expandComment(adapter, realData) {
                        adapter.expandOrCollapse(
                            position = position,
                            animate = false,
                            notify = true,
                            parentPayload = PlayerInfoAdapter.EXPAND_COLLAPSE_PAYLOAD
                        )
                    }
                }
            }
        }
    }
}

class NestedCommentProvider(isAdult: Boolean, listener: PlayerInfoAdapter.PlayerInfoListener) : BaseCommentProvider(isAdult) {
    override val itemViewType: Int
        get() = 2

    override val layoutId: Int
        get() = R.layout.item_comment_nested

    init {
        addChildClickViewIds(R.id.tv_like, R.id.tv_unlike, R.id.btn_reply)
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        dataConvert(helper, (item as NestedCommentNode).data)
    }

    override fun onChildClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {

    }
}

abstract class BaseCommentProvider(private val isAdult: Boolean) : BaseNodeProvider() {

    protected fun dataConvert(holder: BaseViewHolder, data: MembersPostCommentItem) {
        holder.getView<View>(R.id.line_Top).apply {
            if (holder.layoutPosition == 1) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE

                if (isAdult) {
                    setBackgroundResource(R.color.color_white_1_10)
                } else {
                    setBackgroundResource(R.color.color_black_1_05)
                }
            }
        }

        holder.setBackgroundResource(
            R.id.layout_root,
            if (holder.layoutPosition == 1) {
                if (isAdult)
                    R.drawable.bg_adult_comment_top_radius_10
                else
                    R.drawable.bg_comment_top_radius_10
            } else {
                if (isAdult)
                    R.color.color_white_1_10
                else
                    R.color.color_gray_2
            }
        )

        holder.getView<ImageView>(R.id.iv_avatar)

        holder.setText(R.id.tv_name, data.postName)
        holder.setTextColorRes(R.id.tv_name, getTextColor())
        holder.setTextColorRes(R.id.tv_date, getTextColor())
        holder.setText(R.id.tv_message, data.content)
        holder.setTextColorRes(R.id.tv_message, getMessageTextColor())
        holder.setText(R.id.tv_like, (data.likeCount ?: 0L).toString())
        holder.getView<TextView>(R.id.tv_like).setCompoundDrawablesRelativeWithIntrinsicBounds(getLikeRes(data.likeType == 0), 0, 0, 0)
        holder.setTextColorRes(R.id.tv_like, getMessageTextColor())
        holder.setText(R.id.tv_unlike, (data.dislikeCount ?: 0L).toString())
        holder.setTextColorRes(R.id.tv_unlike, getMessageTextColor())
        holder.getView<TextView>(R.id.tv_unlike).setCompoundDrawablesRelativeWithIntrinsicBounds(getDislikeRes(data.likeType == 1), 0, 0, 0)

        //holder.getView<ImageView>(R.id.btn_more)
        holder.setTextColorRes(R.id.btn_reply, getTextColor())

        holder.setGone(R.id.btn_show_comment_reply, true)

        data.creationDate?.let { date ->
            SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault()).format(date)
        }.also {
            holder.setText(R.id.tv_date, it)
        }
    }

    private fun getTextColor() = if (isAdult) R.color.color_white_1_50 else R.color.color_black_1_50

    private fun getMessageTextColor() = if (isAdult) R.color.color_white_1 else R.color.color_black_1

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
}