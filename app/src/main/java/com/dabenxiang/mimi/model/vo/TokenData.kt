package com.dabenxiang.mimi.model.vo

data class TokenData(
    var accessToken: String = "",
    var refreshToken: String = "",
    var expiresTimestamp: Long = 0L
)
