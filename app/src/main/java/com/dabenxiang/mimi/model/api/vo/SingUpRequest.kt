package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class SingUpRequest(
    @SerializedName("username")
    val username: String?,

    @SerializedName("password")
    val password: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("friendlyName")
    val friendlyName: String?,

    @SerializedName("promoCode")
    val promoCode: String?,

    @SerializedName("validationUrl")
    val validationUrl: String?
)