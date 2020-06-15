package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class MemberFollowItem(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("userId")
    val userId: Int?,

    @SerializedName("friendlyName")
    val friendlyName: String?,

    @SerializedName("avatarAttachmentId")
    val avatarAttachmentId: Int?
)