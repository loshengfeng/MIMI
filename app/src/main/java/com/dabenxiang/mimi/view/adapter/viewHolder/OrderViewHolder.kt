package com.dabenxiang.mimi.view.adapter.viewHolder

import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ChatListItem
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.model.enums.OrderStatus
import com.dabenxiang.mimi.model.enums.OrderType
import com.dabenxiang.mimi.model.enums.PaymentType
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.order.OrderFuncItem
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.android.synthetic.main.item_order.view.*
import java.text.SimpleDateFormat
import java.util.*

class OrderViewHolder(view: View) : BaseViewHolder(view) {
    private val tvStatus: TextView = view.tv_status
    private val ivType: ImageView = view.iv_type
    private val clProxy: ConstraintLayout = view.cl_proxy
    private val tvFailureReason: TextView = view.tv_failure_reason
    private val ivAvatar: ImageView = view.img_avatar
    private val tvName: TextView = view.tv_name
    private val tvOrderId: TextView = view.tv_order_id
    private val tvTime: TextView = view.tv_time
    private val tvPoint: TextView = view.tv_point
    private val tvSellingPrice: TextView = view.tv_selling_price
    private val btnContact: Button = view.btn_contact
    private val ivNew: ImageView = view.iv_new
    private val clPaymentInfo: ConstraintLayout = view.cl_payment_info

    private var orderItem: OrderItem? = null

    fun bind(orderItem: OrderItem?, orderFuncItem: OrderFuncItem?) {
        this.orderItem = orderItem

        tvStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
        tvStatus.compoundDrawablePadding = 0

        clPaymentInfo.visibility = View.GONE

        when (orderItem?.status) {
            OrderStatus.PENDING -> {
                tvStatus.setTextColor(tvStatus.context.getColor(R.color.color_black_1))
                tvStatus.text = tvStatus.context.getString(R.string.topup_pending)
                takeIf { orderItem.type == OrderType.USER2ONLINE }?.run {
                    clPaymentInfo.visibility = View.VISIBLE
                }
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
                tvStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ico_attention,
                    0
                )
                tvStatus.compoundDrawablePadding = 4
            }
        }

        when (orderItem?.type) {
            OrderType.USER2ONLINE -> {
                clProxy.visibility = View.GONE
                ivType.visibility = View.VISIBLE
                ivType.setBackgroundResource(
                    when (orderItem.paymentType) {
                        PaymentType.ALI -> R.drawable.ico_alipay
                        PaymentType.WX -> R.drawable.ico_wechat_pay
                        else -> R.drawable.ico_bank
                    }
                )
            }
            else -> {
                ivType.visibility = View.INVISIBLE
                clProxy.visibility = View.VISIBLE
                tvName.text = orderItem?.merchantUserFriendlyName
                orderFuncItem?.getOrderProxyAttachment?.invoke(orderItem?.merchantUserAvatarAttachmentId,ivAvatar)
            }
        }

        orderItem?.failureReason?.takeIf { !TextUtils.isEmpty(it) }?.also {
            tvFailureReason.text = it
            tvFailureReason.visibility = View.VISIBLE
        } ?: run { tvFailureReason.visibility = View.GONE }

        tvOrderId.text = orderItem?.id.toString()

        // 格式為YYYY-MM-DD hh:mm
        tvTime.text = orderItem?.completionTime?.let { date ->
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date)
        } ?: let { tvTime.context.getString(R.string.topup_default_time) }

        // 僅顯示會員充值的蜜幣數量
        tvPoint.text = orderItem?.packagePoint.toString()

        // 若未登入顯示「-」
        tvSellingPrice.text = GeneralUtils.getAmountFormat(orderItem?.sellingPrice ?: 0f)

        btnContact.setOnClickListener {
            orderItem?.also {
                orderFuncItem?.onContactClick?.invoke(
                    ChatListItem(
                        id = it.chatId,
                        name = if (TextUtils.isEmpty(it.merchantUserFriendlyName)) btnContact.context.getString(
                            R.string.order_contact_mimi_service
                        ) else it.merchantUserFriendlyName,
                        avatarAttachmentId = it.merchantUserAvatarAttachmentId,
                        lastReadTime = it.lastReadTime
                    ),
                    orderItem
                )
                it.lastReadTime = Calendar.getInstance().time
                ivNew.visibility = View.GONE
            }
        }

        clPaymentInfo.setOnClickListener {
            orderItem?.also { orderFuncItem?.onPaymentInfoClick?.invoke(it) }
        }

        ivNew.visibility =
            orderItem?.takeIf { it.lastReadTime?.time ?: 0 < it.lastReplyTime?.time ?: 0 }?.let {
                View.VISIBLE
            } ?: let { View.GONE }
    }

    private fun updateAvatar(id: String) {
        val bitmap = LruCacheUtils.getLruCache(id)
        Glide.with(ivAvatar.context).load(bitmap).into(ivAvatar)
    }

}