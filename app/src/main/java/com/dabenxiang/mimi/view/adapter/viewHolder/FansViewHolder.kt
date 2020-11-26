package com.dabenxiang.mimi.view.adapter.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ChatListItem
import com.dabenxiang.mimi.model.api.vo.error.FansItem
import com.dabenxiang.mimi.view.adapter.ChatHistoryAdapter
import com.dabenxiang.mimi.view.adapter.FansListAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import java.text.SimpleDateFormat
import java.util.*

class FansViewHolder(
        itemView: View,
        val listener: FansListAdapter.EventListener
) : BaseAnyViewHolder<FansItem>(itemView) {

    private val imgChatPhoto: ImageView = itemView.findViewById(R.id.imgChatPhoto) as ImageView
    private val imgIsNew: ImageView = itemView.findViewById(R.id.imgIsNew) as ImageView
    private val textName: TextView = itemView.findViewById(R.id.textName) as TextView
    private val textContent: TextView = itemView.findViewById(R.id.textContent) as TextView
    private val textDate: TextView = itemView.findViewById(R.id.textDate) as TextView
    private val btnChatHistory: ConstraintLayout = itemView.findViewById(R.id.btnChatHistory) as ConstraintLayout

    override fun updated(position: Int) {

    }

    override fun updated() {

    }
}