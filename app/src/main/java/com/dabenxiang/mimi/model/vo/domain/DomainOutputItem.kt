package com.dabenxiang.mimi.model.vo.domain

import com.google.gson.annotations.SerializedName

data class DomainOutputItem(

    @SerializedName("id")
    var id: Int = 0,

    @SerializedName("projectID")
    var projectId: String = "",

    @SerializedName("strategyType")
    var strategyType: Int = 0,

    @SerializedName("appID")
    var appId: String = "",

    @SerializedName("type")
    var type: Int = 0,

    @SerializedName("domain")
    val domain: String = "",

    @SerializedName("status")
    var status: Int = 0,

    @SerializedName("note")
    val note: String = "",

    @SerializedName("updatedAt")
    val updatedAt: String = "",

    @SerializedName("createdAt")
    val createdAt: String = "",

    @SerializedName("sortBy")
    var sortBy: Int = 0
)
