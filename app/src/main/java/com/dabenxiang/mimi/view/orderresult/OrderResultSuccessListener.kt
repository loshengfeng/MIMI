package com.dabenxiang.mimi.view.orderresult

import com.dabenxiang.mimi.model.vo.mqtt.OrderPayloadItem

interface OrderResultSuccessListener {
    fun onConfirm()
    fun onClose()
    fun onOpenPaymentWebView(item: OrderPayloadItem?)
}