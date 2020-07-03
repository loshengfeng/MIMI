package com.dabenxiang.mimi.model.vo

data class TokenItem(
    var accessToken: String = "",
    var refreshToken: String = "",
    var expiresTimestamp: Long = 0L
)
