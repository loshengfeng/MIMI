package com.dabenxiang.mimi.model.holder

data class TopupOnlinePayItem(
    val check: Int = 0,
    val token: String?,
    val price: String?,
    val originalPrice: String?
)