package com.dabenxiang.mimi.view.orderresult

import com.dabenxiang.mimi.model.enums.PaymentType
import com.dabenxiang.mimi.model.vo.mqtt.OrderPayloadItem
import com.dabenxiang.mimi.view.base.BaseViewModel

class OrderResultViewModel : BaseViewModel() {

    fun isOpenPaymentWebView(item: OrderPayloadItem?): Boolean {
        return item?.paymentType == PaymentType.ALI.value
                || item?.paymentType == PaymentType.WX.value
    }
}