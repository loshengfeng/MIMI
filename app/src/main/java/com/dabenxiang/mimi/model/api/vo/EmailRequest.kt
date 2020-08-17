package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class EmailRequest(
    @SerializedName("validationUrl")
    val validationUrl: String?
)