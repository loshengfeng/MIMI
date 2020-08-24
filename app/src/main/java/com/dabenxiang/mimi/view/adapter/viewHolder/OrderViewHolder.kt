package com.dabenxiang.mimi.view.adapter.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.model.enums.PaymentType
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_order.view.*
import timber.log.Timber

class OrderViewHolder(view: View) : BaseViewHolder(view) {
    private val ivType: ImageView = view.iv_type
    private val tvAccount: TextView = view.tv_account
    private val tvTime: TextView = view.tv_time
    private val tvToken: TextView = view.tv_token
    private val tvPrice: TextView = view.tv_price
    private val clRoot: ConstraintLayout = view.cl_root

    private var orderItem: OrderItem? = null

    init {
        Timber.d("@@init")
        view.setOnClickListener {
            Timber.d("@@nClick")
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
        ivType.setOnClickListener {
            Timber.d("@@ivType setOnClickListener")
        }

        clRoot.setOnClickListener {
            Timber.d("@@setOnClickListener")
        }

        tvAccount.text = orderItem?.accountName

        // 格式為YYYY-MM-DD hh:mm
        tvTime.text = orderItem?.completionTime

        // 僅顯示會員充值的蜜幣數量
        tvToken.text = orderItem?.packagePoint.toString()

        // 若未登入顯示「-」
        tvPrice.text = orderItem?.sellingPrice.toString()
    }

}