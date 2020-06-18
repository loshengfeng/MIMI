package com.dabenxiang.mimi.view.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.chad.library.adapter.base.loadmore.BaseLoadMoreView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.extension.setBtnSolidDolor
import com.yulichswift.roundedview.widget.RoundedTextView

class CommentLoadMoreView(private val isAdult: Boolean) : BaseLoadMoreView() {

    override fun getRootView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.layout_comment_load_more, parent, false)
    }

    override fun getLoadingView(holder: BaseViewHolder): View {
        return holder.getView(R.id.view_load_more_loading)
    }

    override fun getLoadComplete(holder: BaseViewHolder): View {
        setTextColor(holder.getView(R.id.tv_load_more_complete))

        val bgView = holder.getView<RoundedTextView>(R.id.bg_load_more_complete)
        val res = bgView.resources
        val bgColor = res.getColor(if (isAdult) R.color.color_white_1_10 else R.color.color_black_1_05, null)
        bgView.setBtnSolidDolor(bgColor, -1, res.getDimension(R.dimen.dp_4))

        val textView = holder.getView<TextView>(R.id.tv_load_more_complete)
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
            0,
            0,
            if (isAdult) R.drawable.btn_arrowdown_white_n else R.drawable.btn_arrowdown_gray_n,
            0
        )

        return holder.getView(R.id.view_load_more_complete)
    }

    override fun getLoadEndView(holder: BaseViewHolder): View {
        return setTextColor(holder.getView(R.id.view_load_more_end))
    }

    override fun getLoadFailView(holder: BaseViewHolder): View {
        return setTextColor(holder.getView(R.id.view_load_more_fail))
    }

    private fun setTextColor(textView: TextView): TextView {
        val textColor = if (isAdult) {
            R.color.color_white_1_30
        } else {
            R.color.color_black_1_50
        }.let {
            textView.resources.getColor(it, null)
        }
        textView.setTextColor(textColor)
        return textView
    }
}