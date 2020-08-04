package com.dabenxiang.mimi.model.vo

data class TopUpOnlinePayItem(
    val check: Int = 0,
    val token: String?,
    val price: String?,
    val originalPrice: String?
)