package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ValidateMessageRequest(
    @SerializedName("mobileNum")
    val mobile: String
)