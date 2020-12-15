package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class SingUpRequest(
    @SerializedName("username")
    val username: String?,

    @SerializedName("password")
    val password: String?="",

    @SerializedName("friendlyName")
    val friendlyName: String?,

    @SerializedName("referrerCode")
    val referrerCode: String?,

    @SerializedName("code")
    val code: String?
)