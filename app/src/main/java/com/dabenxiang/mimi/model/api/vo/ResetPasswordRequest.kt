package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ResetPasswordRequest(
    @SerializedName("username")
    val username: String?,

    @SerializedName("password")
    val password: String?
)