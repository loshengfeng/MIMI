package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class PagingItem(

    @SerializedName("offset")
    val offset: Long,

    @SerializedName("limit")
    val limit: Long,

    @SerializedName("pages")
    val pages: Long,

    @SerializedName("count")
    val count: Long,

    @SerializedName("pageIndex")
    val pageIndex: Long,

    @SerializedName("pageSize")
    val pageSize: Long,

    @SerializedName("pageCount")
    val pageCount: Long,

    @SerializedName("recordCount")
    val recordCount: Long
)