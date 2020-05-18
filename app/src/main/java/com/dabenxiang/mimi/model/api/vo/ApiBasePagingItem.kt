package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ApiBasePagingItem<T>(
    @SerializedName("code")
    val code: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("content")
    val content: T?,

    @SerializedName("paging")
    val paging: PagingItem
)
