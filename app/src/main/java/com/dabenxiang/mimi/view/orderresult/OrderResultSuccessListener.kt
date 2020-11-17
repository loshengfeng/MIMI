package com.dabenxiang.mimi.view.orderresult

interface OrderResultSuccessListener {
    fun onConfirm()
    fun onClose()
    fun onOpenPaymentWebView(url: String)
}