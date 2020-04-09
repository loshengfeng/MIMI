package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class PagingItem(
    @SerializedName("limit")
    val limit: Int?,

    @SerializedName("offset")
    val offset: Int?,

    @SerializedName("pages")
    val pages: Int?,

    @SerializedName("count")
    val count: Int?
)