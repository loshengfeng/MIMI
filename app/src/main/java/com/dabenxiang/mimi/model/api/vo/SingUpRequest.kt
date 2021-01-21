package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName


data class SingUpGuestRequest(
    @SerializedName("referrerCode")
    val referrerCode: String? = "",

    @SerializedName("deviceId")
    val deviceId: String? = ""
)

data class SingInRequest(
    @SerializedName("id")
    val id: Long? = 0,

    @SerializedName("username")
    val username: String? = "",

    @SerializedName("code")
    val code: String? = ""
)

data class BindPhoneRequest(
    @SerializedName("username")
    val username: String? = "",

    @SerializedName("friendlyName")
    val friendlyName: String? = "",

    @SerializedName("referrerCode")
    val referrerCode: String? = "",

    @SerializedName("code")
    val code: String? = ""
)