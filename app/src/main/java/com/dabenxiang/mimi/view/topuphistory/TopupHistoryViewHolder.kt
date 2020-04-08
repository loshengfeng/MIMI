package com.dabenxiang.mimi.view.topuphistory

import android.view.View
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_topup_history.view.*

class TopupHistoryViewHolder(view: View) : BaseViewHolder(view) {
    val ivType = itemView.iv_type!!
    val tvAccount = itemView.tv_account!!
    val tvTime = itemView.tv_time!!
    val tvToken = itemView.tv_token!!
    val tvPrice = itemView.tv_price!!
}