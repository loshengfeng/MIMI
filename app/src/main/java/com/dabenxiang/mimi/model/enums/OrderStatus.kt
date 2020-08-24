package com.dabenxiang.mimi.model.enums

enum class OrderStatus(val value: Int) {
    PENDING(0), //处理中
    TRANSACTION(1),
    COMPLETED(2), //交易完成
    ORDER_CREATING(10), //订单建立中
    ORDER_CREATE_FAIL(15), //订单建立失败
    CANCELED(44),
    FAILED(99) //交易失败
}