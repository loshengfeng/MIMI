package com.dabenxiang.mimi.model.api.vo.error

import com.google.gson.annotations.SerializedName

data class ErrorItem(

    @SerializedName("code")
    var code: String?,

    @SerializedName("message")
    var message: String?,

    @SerializedName("details")
    var details: String?
)
