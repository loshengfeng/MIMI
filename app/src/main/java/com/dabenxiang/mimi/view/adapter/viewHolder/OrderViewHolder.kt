package com.dabenxiang.mimi.view.adapter.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.OrderItem
import timber.log.Timber

class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val ivType = itemView.findViewById(R.id.iv_type) as ImageView
    private val tvAccount = itemView.findViewById(R.id.tv_account) as TextView
    private val tvTime = itemView.findViewById(R.id.tv_time) as TextView
    private val tvToken = itemView.findViewById(R.id.tv_token) as TextView
    private val tvPrice = itemView.findViewById(R.id.tv_price) as TextView

    private var orderItem: OrderItem?= null

    init {
        view.setOnClickListener {
            // todo : not sure...
            Timber.d("onClick")
        }
    }

    companion object {
        fun create(parent: ViewGroup): ClubFollowViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
            return ClubFollowViewHolder(view)
        }
        const val NONE = 0
        const val ALIPAY = 1
        const val WECHAT = 2
        const val UNIONPAY = 4
    }

    fun bind(orderItem: OrderItem?) {
        this.orderItem = orderItem
        ivType.setBackgroundResource(when(orderItem?.paymentType) {
            NONE -> R.drawable.ico_alipay
            ALIPAY -> R.drawable.ico_alipay
            WECHAT -> R.drawable.ico_wechat_pay
            UNIONPAY -> R.drawable.ico_china_pay
            else -> R.drawable.ico_alipay
        })

        tvAccount.text = orderItem?.accountName

        // 格式為YYYY-MM-DD hh:mm
        tvTime.text = orderItem?.completionTime

        // 僅顯示會員充值的蜜幣數量
        tvToken.text = orderItem?.packagePoint.toString()

        // 若未登入顯示「-」
        tvPrice.text = orderItem?.sellingPrice.toString()
    }

}