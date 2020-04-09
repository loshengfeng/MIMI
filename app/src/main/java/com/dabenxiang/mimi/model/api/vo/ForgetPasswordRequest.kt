package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ForgetPasswordRequest(
    @SerializedName("username")
    val username: String?,

    @SerializedName("email")
    val email: String?
)