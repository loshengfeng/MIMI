package com.dabenxiang.mimi.view.actress

import android.view.View
import android.widget.TextView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ReferrerHistoryItem
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import java.text.SimpleDateFormat
import java.util.*

class ActressViewHolder(
        itemView: View,
        val listener: ActressAdapter.EventListener
) : BaseAnyViewHolder<ReferrerHistoryItem>(itemView) {

    private val textName: TextView = itemView.findViewById(R.id.tv_invite_name) as TextView
    private val textPhone: TextView = itemView.findViewById(R.id.tv_invite_phone) as TextView
    private val textDate: TextView = itemView.findViewById(R.id.tv_invite_date) as TextView

    override fun updated(position: Int) {
        textName.text = data?.friendlyName
        textPhone.text = data?.username
        textDate.text = data?.creationDate.let { date -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date) }
    }

    override fun updated() {

    }
}