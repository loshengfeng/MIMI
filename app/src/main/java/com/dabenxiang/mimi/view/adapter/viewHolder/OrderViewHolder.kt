package com.dabenxiang.mimi.view.adapter.viewHolder

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.model.enums.OrderStatus
import com.dabenxiang.mimi.model.enums.PaymentType
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_order.view.*
import timber.log.Timber

class OrderViewHolder(view: View) : BaseViewHolder(view) {
    private val tvStatus: TextView = view.tv_status
    private val ivType: ImageView = view.iv_type
    private val tvOrderId: TextView = view.tv_order_id
    private val tvTime: TextView = view.tv_time
    private val tvPoint: TextView = view.tv_point
    private val tvSellingPrice: TextView = view.tv_selling_price
    private val clRoot: ConstraintLayout = view.cl_root

    private var orderItem: OrderItem? = null

    fun bind(orderItem: OrderItem?) {
        this.orderItem = orderItem

        when(orderItem?.status) {
            OrderStatus.PENDING -> {
                tvStatus.setTextColor(tvStatus.context.getColor(R.color.color_black_1))
                tvStatus.text = tvStatus.context.getString(R.string.topup_pending)
            }
            OrderStatus.TRANSACTION -> {
                tvStatus.setTextColor(tvStatus.context.getColor(R.color.color_black_1))
                tvStatus.text = tvStatus.context.getString(R.string.topup_transaction)
            }
            OrderStatus.COMPLETED -> {
                tvStatus.setTextColor(tvStatus.context.getColor(R.color.color_green_1))
                tvStatus.text = tvStatus.context.getString(R.string.topup_completed)
            }
            OrderStatus.ORDER_CREATING -> {
                tvStatus.setTextColor(tvStatus.context.getColor(R.color.color_black_1))
                tvStatus.text = tvStatus.context.getString(R.string.topup_order_creating)
            }
            OrderStatus.ORDER_CREATE_FAIL -> {
                tvStatus.setTextColor(tvStatus.context.getColor(R.color.color_black_1))
                tvStatus.text = tvStatus.context.getString(R.string.topup_order_create_fail)
            }
            OrderStatus.CANCELED -> {
                tvStatus.setTextColor(tvStatus.context.getColor(R.color.color_black_1))
                tvStatus.text = tvStatus.context.getString(R.string.topup_canceled)
            }
            OrderStatus.FAILED -> {
                tvStatus.setTextColor(tvStatus.context.getColor(R.color.color_red_1))
                tvStatus.text = tvStatus.context.getString(R.string.topup_failed)
            }
        }

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

        tvOrderId.text = orderItem?.id.toString()

        // 格式為YYYY-MM-DD hh:mm
        Timber.d("@@completionTime: ${orderItem?.completionTime}")
        tvTime.text = orderItem?.completionTime
            ?: let { tvTime.context.getString(R.string.topup_default_time) }

        // 僅顯示會員充值的蜜幣數量
        tvPoint.text = orderItem?.packagePoint.toString()

        // 若未登入顯示「-」
        tvSellingPrice.text = orderItem?.sellingPrice.toString()
    }

}