package com.dabenxiang.mimi.view.orderresult

import android.text.TextUtils
import com.dabenxiang.mimi.model.vo.mqtt.OrderPayloadItem
import com.dabenxiang.mimi.view.base.BaseViewModel

class OrderResultViewModel : BaseViewModel() {

    private var orderPayloadItem: OrderPayloadItem? = null

    fun isOpenPaymentWebView(item: OrderPayloadItem?): Boolean {
        return !TextUtils.isEmpty(item?.paymentUrl)
    }

    fun setupOrderPayloadItem(item: OrderPayloadItem?) {
        orderPayloadItem = item
    }

    fun getOrderPayloadItem(): OrderPayloadItem? {
        return orderPayloadItem
    }
}