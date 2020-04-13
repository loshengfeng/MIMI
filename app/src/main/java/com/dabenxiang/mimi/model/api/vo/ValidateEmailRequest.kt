package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ValidateEmailRequest(
    @SerializedName("userId")
    val userId: Int?,

    @SerializedName("validationUrl")
    val validationUrl: String?
)