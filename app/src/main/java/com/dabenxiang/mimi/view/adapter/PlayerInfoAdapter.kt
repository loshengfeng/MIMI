package com.dabenxiang.mimi.view.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseBinderAdapter
import com.chad.library.adapter.base.binder.QuickItemBinder
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.extension.setBtnSolidColor
import com.dabenxiang.mimi.model.api.vo.MembersPostCommentItem
import com.yulichswift.roundedview.widget.RoundedTextView
import java.text.SimpleDateFormat
import java.util.*

class PlayerInfoAdapter(isAdult: Boolean) : BaseBinderAdapter(), LoadMoreModule {

    init {
        addItemBinder(MembersPostCommentItem::class.java, MembersPostCommentItemBinder(isAdult))
    }
}

class MembersPostCommentItemBinder(private val isAdult: Boolean) : QuickItemBinder<MembersPostCommentItem>() {
    override fun getLayoutId() = R.layout.item_comment_root
    override fun convert(holder: BaseViewHolder, data: MembersPostCommentItem) {

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

        holder.getView<RoundedTextView>(R.id.btn_show_comment_reply).also {
            if (data.commentCount != null && data.commentCount > 0) {
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

                it.setBtnSolidColor(solidColor, pressedColor, it.resources.getDimension(R.dimen.dp_10))
                it.text = String.format(it.resources.getString(R.string.n_reply), data.commentCount)

                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }

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