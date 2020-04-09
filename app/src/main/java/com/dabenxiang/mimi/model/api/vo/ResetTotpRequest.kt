package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ResetTotpRequest(
    @SerializedName("username")
    val userName: String?,

    @SerializedName("friendlyName")
    val friendlyName: String?
)