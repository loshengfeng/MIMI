package com.dabenxiang.mimi.view.adapter.viewHolder.chat

import android.content.res.ColorStateList
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ChatContentItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.FunctionType
import com.dabenxiang.mimi.view.adapter.ChatContentAdapter
import com.dabenxiang.mimi.view.adapter.SearchVideoAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.head_video_info.view.*

class ChatContentDateTitleViewHolder(
        itemView: View,
        val listener: ChatContentAdapter.EventListener
) : BaseAnyViewHolder<ChatContentItem>(itemView) {

    private val tvDate = itemView.findViewById(R.id.txt_date) as TextView

    init {
    }

    override fun updated() {
        tvDate.text = data?.dateTitle
    }
}