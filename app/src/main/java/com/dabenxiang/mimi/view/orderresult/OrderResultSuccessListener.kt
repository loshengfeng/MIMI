package com.dabenxiang.mimi.view.orderresult

interface OrderResultSuccessListener {
    fun onConfirm()
    fun onClose()
    fun onOpenWebView(url: String)
}