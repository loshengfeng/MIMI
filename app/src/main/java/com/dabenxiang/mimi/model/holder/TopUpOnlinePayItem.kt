package com.dabenxiang.mimi.model.holder

data class TopUpOnlinePayItem(
    val check: Int = 0,
    val token: String?,
    val price: String?,
    val originalPrice: String?
)