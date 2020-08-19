package com.dabenxiang.mimi.view.orderresult

import com.airbnb.epoxy.Typed2EpoxyController
import com.dabenxiang.mimi.view.orderresult.itemview.*

class OrderResultEpoxyController(
    private val failedListener: OrderResultFailedItemView.OrderResultFailedListener,
    private val successListener: OrderResultSuccessItemView.OrderResultSuccessListener
) : Typed2EpoxyController<String, String>() {

    override fun buildModels(data: String?, epoxyState: String?) {

//        addOrderResultWaitingItemView()
//        addOrderResultFailedItemView()
        addOrderResultSuccessfulItemView()

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

    private fun addOrderResultSuccessfulItemView() {
        orderResultSuccessItemView {
            id("order_result_success")
            setupTimeout("请于 YYYY-MM-DD hh:mm 前完成打款动作，避免订单超时")
            setupName("王大明")
            setupBank("中国银行(111) 南京分行")
            setupCity("开户省/开户市/开户行")
            setupAccount("1234567890123456")
            setupAmount("100.00")
            setupClickListener(successListener)
        }
    }
}