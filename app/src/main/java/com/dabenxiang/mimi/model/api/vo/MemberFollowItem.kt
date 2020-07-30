package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class MemberFollowItem(
    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("userId")
    val userId: Long = 0,

    @SerializedName("friendlyName")
    val friendlyName: String = "",

    @SerializedName("avatarAttachmentId")
    val avatarAttachmentId: Long = 0
)