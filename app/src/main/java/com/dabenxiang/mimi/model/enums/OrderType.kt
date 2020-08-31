package com.dabenxiang.mimi.model.enums

enum class OrderType(val value: Int) {
    OP2MERCHANT(1),
    MERCHANT2USER(2),
    USER2ONLINE(4),
    OP2MEMBER(8),
    SYSTEMBONUS(16)
}