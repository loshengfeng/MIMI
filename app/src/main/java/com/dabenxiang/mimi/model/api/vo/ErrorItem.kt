package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ErrorItem(

    @SerializedName("code")
    var code: String? = null,

    @SerializedName("message")
    var message: String? = null,

    @SerializedName("details")
    var details: String? = null
)
