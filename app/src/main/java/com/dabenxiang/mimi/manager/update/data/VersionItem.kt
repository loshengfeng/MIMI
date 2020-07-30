package com.dabenxiang.mimi.manager.update.data

import com.google.gson.annotations.SerializedName

data class VersionItem(

    @SerializedName("code")
    val code: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("major")
    val major: Long = 0,

    @SerializedName("stage") // 0：Prod | 1：Dev | 2：Sit | 3：Alpha | 4：Beta
    val stage: Int = 0,

    @SerializedName("releaseId")
    val releaseId: Long = 0,

    @SerializedName("state")
    val state: String = "0"
)
