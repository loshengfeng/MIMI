package com.dabenxiang.mimi.view.orderresult

import android.content.Context
import android.text.TextUtils
import com.airbnb.epoxy.TypedEpoxyController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.enums.PaymentType
import com.dabenxiang.mimi.model.vo.mqtt.OrderPayloadItem
import com.dabenxiang.mimi.view.orderresult.itemview.orderResultDetailSuccessItemView
import com.dabenxiang.mimi.view.orderresult.itemview.orderResultFailedItemView
import com.dabenxiang.mimi.view.orderresult.itemview.orderResultUrlSuccessItemView
import com.dabenxiang.mimi.view.orderresult.itemview.orderResultWaitingItemView
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import java.text.SimpleDateFormat
import java.util.*

class OrderResultEpoxyController(
    val context: Context,
    private val failedListener: OrderResultFailedListener,
    private val successListener: OrderResultSuccessListener
) : TypedEpoxyController<OrderPayloadItem>() {

    override fun buildModels(item: OrderPayloadItem?) {
        if (item == null) {
            addOrderResultWaitingItemView()
        } else {
            if (item.isSuccessful) {
                addOrderResultSuccessfulItemView(item)
            } else {
                addOrderResultFailedItemView()
            }
        }
    }

    private fun addOrderResultWaitingItemView() {
        orderResultWaitingItemView {
            id("order_result_waiting")
        }
    }

    private fun addOrderResultFailedItemView() {
        orderResultFailedItemView {
            id("order_result_failed")
            setupClickListener(failedListener)
        }
    }

    private fun addOrderResultSuccessfulItemView(item: OrderPayloadItem) {

        val calendar = Calendar.getInstance()
        calendar.time = item.createTime ?: Date()
        calendar.add(Calendar.HOUR_OF_DAY, 1)
        val sdf = SimpleDateFormat("YYYY-MM-dd HH:mm")
        val time = sdf.format(calendar.time)

        val timeout = StringBuilder("请于 ")
            .append(time)
            .append(" 前完成打款动作，避免订单超时")
            .toString()

        val bank = StringBuilder(item.bankName)
            .append("(")
            .append(item.bankCode)
            .append(") ")
            .toString()

        val city = StringBuilder(item.bankBranchProvince)
            .append("/")
            .append(item.bankBranchCity)
            .append("/")
            .append(item.bankBranchName)
            .toString()

        when (item.paymentType) {
            PaymentType.BANK.value -> {
                if (TextUtils.isEmpty(item.paymentUrl)) {
                    orderResultDetailSuccessItemView {
                        id("order_result_detail_success")
                        setupTimeout(timeout)
                        setupName(item.accountName)
                        setupBank(bank)
                        setupCity(city)
                        setupAccount(item.accountNumber)
                        setupAmount(GeneralUtils.getAmountFormat(item.amount))
                        setupClickListener(successListener)
                    }
                } else {
                    orderResultUrlSuccessItemView {
                        id("order_result_url_bank_success")
                        setupTimeout(timeout)
                        setupPaymentImg(R.drawable.ico_bank_160_px)
                        setupPaymentCountdown(item.countdown)
                        setupPaymentCountdownColor(R.color.color_black_1)
                        setupPaymentCountdownBackground(R.drawable.bg_black_1_radius_6)
                        setupPaymentGoBackground(R.drawable.bg_black_2_radius_6)
                        setupPaymentCountdownVisibility(item.isCountdownVisible)
                        setupAmount(GeneralUtils.getAmountFormat(item.amount))
                        setupPaymentPageListener(item.paymentUrl)
                        setupClickListener(successListener)
                    }
                }
            }
            PaymentType.ALI.value -> {
                orderResultUrlSuccessItemView {
                    id("order_result_url_ali_success")
                    setupTimeout(timeout)
                    setupPaymentImg(R.drawable.ico_alipay_160_px)
                    setupPaymentCountdown(item.countdown)
                    setupPaymentCountdownColor(R.color.color_blue_3)
                    setupPaymentCountdownBackground(R.drawable.bg_blue_1_radius_6)
                    setupPaymentGoBackground(R.drawable.bg_blue_2_radius_6)
                    setupPaymentCountdownVisibility(item.isCountdownVisible)
                    setupAmount(GeneralUtils.getAmountFormat(item.amount))
                    setupPaymentPageListener(item.paymentUrl)
                    setupClickListener(successListener)
                }
            }
            PaymentType.WX.value -> {
                orderResultUrlSuccessItemView {
                    id("order_result_url_wx_success")
                    setupTimeout(timeout)
                    setupPaymentImg(R.drawable.ico_wechat_pay_160_px)
                    setupPaymentCountdown(item.countdown)
                    setupPaymentCountdownColor(R.color.color_green_2)
                    setupPaymentCountdownBackground(R.drawable.bg_green_1_radius_6)
                    setupPaymentGoBackground(R.drawable.bg_green_2_radius_6)
                    setupPaymentCountdownVisibility(item.isCountdownVisible)
                    setupAmount(GeneralUtils.getAmountFormat(item.amount))
                    setupPaymentPageListener(item.paymentUrl)
                    setupClickListener(successListener)
                }
            }
            PaymentType.TIK_TOK.value -> {
                orderResultUrlSuccessItemView {
                    id("order_result_url_tik_tok_success")
                    setupTimeout(timeout)
                    setupPaymentImg(R.drawable.ico_tiktokpay_160_px)
                    setupPaymentCountdown(item.countdown)
                    setupPaymentCountdownColor(R.color.color_black_1_50)
                    setupPaymentCountdownBackground(R.drawable.bg_black_1_radius_6)
                    setupPaymentGoBackground(R.drawable.bg_black_2_radius_6)
                    setupPaymentCountdownVisibility(item.isCountdownVisible)
                    setupAmount(GeneralUtils.getAmountFormat(item.amount))
                    setupPaymentPageListener(item.paymentUrl)
                    setupClickListener(successListener)
                }
            }
        }
    }
}