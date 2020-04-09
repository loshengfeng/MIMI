package com.dabenxiang.mimi.view.topup

import android.view.View
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_topup_online_pay.view.*

class TopupOnlinePayViewHolder(view: View) : BaseViewHolder(view) {
    val ivCheck = itemView.iv_check!!
    val tvToken = itemView.tv_token!!
    val tvOriginalPrice = itemView.tv_original_price!!
    val tvPrice = itemView.tv_price!!
}