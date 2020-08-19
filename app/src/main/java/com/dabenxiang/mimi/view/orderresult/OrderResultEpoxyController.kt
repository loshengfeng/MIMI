package com.dabenxiang.mimi.view.orderresult

import com.airbnb.epoxy.TypedEpoxyController
import com.dabenxiang.mimi.model.vo.mqtt.OrderItem
import com.dabenxiang.mimi.view.orderresult.itemview.*

class OrderResultEpoxyController(
    private val failedListener: OrderResultFailedItemView.OrderResultFailedListener,
    private val successListener: OrderResultSuccessItemView.OrderResultSuccessListener
) : TypedEpoxyController<OrderItem>() {

    override fun buildModels(item: OrderItem?) {
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

    private fun addOrderResultSuccessfulItemView(item: OrderItem) {
        val timeout = StringBuilder("请于 ")
            .append(item.createTime)
            .append(" 前完成打款动作，避免订单超时")
            .toString()

        val bank = StringBuilder(item.bankBranchName)
            .append("(")
            .append(item.bankCode)
            .append(") ")
            .append(item.bankBranchName)
            .toString()

        val city = StringBuilder(item.bankBranchProvince)
            .append("/")
            .append(item.bankBranchCity)
            .append("/")
            .append(item.bankBranchName)
            .toString()

        orderResultSuccessItemView {
            id("order_result_success")
            setupTimeout(timeout)
            setupName(item.accountName)
            setupBank(bank)
            setupCity(city)
            setupAccount(item.accountNumber)
            setupAmount(item.amount.toString())
            setupClickListener(successListener)
        }
    }
}