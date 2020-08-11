package com.dabenxiang.mimi.model.enums

enum class PaymentStatus(val value: Int) {
    UNPAID(0),
    PAID(1),
    FAILED(99)
}