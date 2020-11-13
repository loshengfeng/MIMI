package com.dabenxiang.mimi.view.orderresult

import com.airbnb.epoxy.TypedEpoxyController
import com.dabenxiang.mimi.model.enums.PaymentType
import com.dabenxiang.mimi.model.vo.mqtt.OrderPayloadItem
import com.dabenxiang.mimi.view.orderresult.itemview.OrderResultFailedItemView
import com.dabenxiang.mimi.view.orderresult.itemview.orderResultBankSuccessItemView
import com.dabenxiang.mimi.view.orderresult.itemview.orderResultFailedItemView
import com.dabenxiang.mimi.view.orderresult.itemview.orderResultWaitingItemView
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import java.text.SimpleDateFormat
import java.util.*

class OrderResultEpoxyController(
    private val failedListener: OrderResultFailedItemView.OrderResultFailedListener,
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
                orderResultBankSuccessItemView {
                    id("order_result_bank_success")
                    setupTimeout(timeout)
                    setupName(item.accountName)
                    setupBank(bank)
                    setupCity(city)
                    setupAccount(item.accountNumber)
                    setupAmount(GeneralUtils.getAmountFormat(item.amount))
                    setupClickListener(successListener)
                }
            }
            else -> successListener.onAliWxConfirm(item.paymentUrl)
        }
    }
}