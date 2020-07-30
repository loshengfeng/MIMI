package com.dabenxiang.mimi.manager.update.data

import com.google.gson.annotations.SerializedName

data class ResultItem(

    @SerializedName("code")
    val code: String = "",

    @SerializedName("message")
    val message: String = ""
)
