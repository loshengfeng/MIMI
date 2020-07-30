package com.dabenxiang.mimi.manager.update.data

import com.google.gson.annotations.SerializedName

data class CompatibilityItem(

    @SerializedName("code")
    val code: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("major")
    val major: Int = 0,

    @SerializedName("stage") // 0：Prod | 1：Dev | 2：Sit | 3：Alpha | 4：Beta
    val stage: Int = 0,

    @SerializedName("releaseId")
    val releaseId: Long = 0
)
