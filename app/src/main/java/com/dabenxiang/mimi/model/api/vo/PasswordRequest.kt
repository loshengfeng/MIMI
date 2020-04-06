package com.dabenxiang.mimi.model.api.vo
import com.google.gson.annotations.SerializedName

data class PasswordRequest(
    @SerializedName("password")
    val password: String,

    @SerializedName("username")
    val username: String
)