package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName
import java.util.*

class ReferrerHistoryItem(
    @SerializedName("friendlyName")
    val friendlyName: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("creationDate")
    val creationDate: Date = Date()
)