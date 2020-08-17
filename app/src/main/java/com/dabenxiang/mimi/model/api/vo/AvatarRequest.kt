package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class AvatarRequest(
    @SerializedName("avatarId")
    val avatarId: Long?
)