package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class MeItem(
    @SerializedName("friendlyName")
    val friendlyName: String?,

    @SerializedName("availablePoint")
    val availablePoint: Int?,

    @SerializedName("hasNewMessage")
    val hasNewMessage: Boolean?,

    @SerializedName("avatarAttachmentId")
    val avatarAttachmentId: Int?
)
