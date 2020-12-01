package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class FansItem(
    @SerializedName("id")
    val id: Long,

    @SerializedName("userId")
    val userId: Long,

    @SerializedName("friendlyName")
    val friendlyName: String? = null,

    @SerializedName("avatarAttachmentId")
    val avatarAttachmentId: Long? = null,

    @SerializedName("isFollow")
    val isFollow: Boolean
)

