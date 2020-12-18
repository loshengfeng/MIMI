package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class AnnounceConfigItem(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("group")
    val group: String = "",

    @SerializedName("value")
    val value: String = "",

    @SerializedName("status")
    val status: Int = 0
)