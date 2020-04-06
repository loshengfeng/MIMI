package com.dabenxiang.mimi.model.api.vo
import com.google.gson.annotations.SerializedName

data class ResetTotpRequest(
    @SerializedName("friendlyName")
    val friendlyName: String,

    @SerializedName("username")
    val username: String
)