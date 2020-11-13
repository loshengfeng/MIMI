package com.dabenxiang.mimi.view.orderresult

interface OrderResultSuccessListener {
    fun onBankConfirm()
    fun onClose()
    fun onAliWxConfirm(url: String)
}