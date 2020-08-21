package com.dabenxiang.mimi.view.adapter.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.model.enums.PaymentType
import com.dabenxiang.mimi.view.base.BaseViewHolder
import timber.log.Timber

class OrderViewHolder(view: View) : BaseViewHolder(view) {
    private val ivType = itemView.findViewById(R.id.iv_type) as ImageView
    private val tvAccount = itemView.findViewById(R.id.tv_account) as TextView
    private val tvTime = itemView.findViewById(R.id.tv_time) as TextView
    private val tvToken = itemView.findViewById(R.id.tv_token) as TextView
    private val tvPrice = itemView.findViewById(R.id.tv_price) as TextView

    private var orderItem: OrderItem? = null

    init {
        view.setOnClickListener {
            Timber.d("onClick")
        }
    }

    fun bind(orderItem: OrderItem?) {
        this.orderItem = orderItem
        ivType.setBackgroundResource(
            when (orderItem?.paymentType) {
                PaymentType.ALI -> R.drawable.ico_alipay
                PaymentType.WX -> R.drawable.ico_wechat_pay
                else -> R.drawable.ico_bank
            }
        )

        tvAccount.text = orderItem?.accountName

        // 格式為YYYY-MM-DD hh:mm
        tvTime.text = orderItem?.completionTime

        // 僅顯示會員充值的蜜幣數量
        tvToken.text = orderItem?.packagePoint.toString()

        // 若未登入顯示「-」
        tvPrice.text = orderItem?.sellingPrice.toString()
    }

}