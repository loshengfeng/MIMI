package com.dabenxiang.mimi.model.api.vo


data class SingUpRequest(
    val username: String?,
    val password: String?,
    val email: String?,
    val friendlyName: String?,
    val promoCode: String?,
    val validationUrl: String?
)