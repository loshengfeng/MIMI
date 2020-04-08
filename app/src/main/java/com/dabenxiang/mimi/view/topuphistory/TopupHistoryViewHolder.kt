package com.dabenxiang.mimi.view.topuphistory

import android.view.View
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_topup_history.view.*

class TopupHistoryViewHolder(view: View) : BaseViewHolder(view) {
    val ivType = itemView.iv_type!!
    val tvAccount = itemView.tv_account!!
    val tvTime = itemView.tv_time!!
    val tvMoney = itemView.tv_money!!
    val tvMoney2 = itemView.tv_money_2!!
}