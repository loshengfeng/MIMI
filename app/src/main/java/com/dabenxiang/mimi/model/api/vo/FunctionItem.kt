package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class FunctionItem(
    @SerializedName("id")
    val id: String?,

    // 1:Read 2:Write 3: All
    @SerializedName("nativePermission")
    val nativePermission: Int?
)