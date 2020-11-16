package com.dabenxiang.mimi.view.orderresult

import android.text.TextUtils
import com.dabenxiang.mimi.model.vo.mqtt.OrderPayloadItem
import com.dabenxiang.mimi.view.base.BaseViewModel

class OrderResultViewModel : BaseViewModel() {

    fun isOpenPaymentWebView(item: OrderPayloadItem?): Boolean {
        return !TextUtils.isEmpty(item?.paymentUrl)
    }
}