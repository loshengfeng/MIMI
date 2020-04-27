package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class PagingItem(
    @SerializedName("limit")
    val limit: Long?,

    @SerializedName("offset")
    val offset: Long?,

    @SerializedName("pages")
    val pages: Long?,

    @SerializedName("count")
    val count: Long?
)