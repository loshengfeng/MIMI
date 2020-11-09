package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class LogApiItem(
    @SerializedName("userId")
    var userId: String? = "",
    @SerializedName("url")
    var url: String? = "",
    @SerializedName("method")
    var method: String? = "",
    @SerializedName("request_headers")
    var requestHeaders: ArrayList<String>? = arrayListOf(),
    @SerializedName("request_body")
    var requestBody: String? = "",
    @SerializedName("response_headers")
    var responseHeaders: ArrayList<String>? = arrayListOf(),
    @SerializedName("response_code")
    var responseCode: String? = "",
    @SerializedName("response_body")
    var responseBody: String? = "",
    @SerializedName("exception")
    var exception: String? = ""
)