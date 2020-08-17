package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ChangePasswordRequest(
    @SerializedName("oldPassword")
    val oldPassword: String?,

    @SerializedName("newPassword")
    val newPassword: String?
)