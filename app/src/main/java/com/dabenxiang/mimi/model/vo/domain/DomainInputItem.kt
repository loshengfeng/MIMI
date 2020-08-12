package com.dabenxiang.mimi.model.vo.domain

import com.google.gson.annotations.SerializedName

data class DomainInputItem(
    @SerializedName("projectID")
    val projectId: String,

    @SerializedName("sqlDirectory")
    val sqlDirectory: String,

    @SerializedName("env")
    val env: String
)