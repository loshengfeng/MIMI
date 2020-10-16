package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class MeItem(
    @SerializedName("id")
    val id: Long?,

    @SerializedName("friendlyName")
    val friendlyName: String?,

    @SerializedName("availablePoint")
    val availablePoint: Int?,

    @SerializedName("hasNewMessage")
    val hasNewMessage: Boolean?,

    @SerializedName("promoCode")
    val promoCode: String?,

    @SerializedName("isEmailConfirmed")
    val isEmailConfirmed: Boolean?,

    @SerializedName("avatarAttachmentId")
    val avatarAttachmentId: Boolean?,

    @SerializedName("IsSubscribed")
    val IsSubscribed: Boolean?,

    @SerializedName("ExpiryDate")
    val ExpiryDate: Long?,

    @SerializedName("VideoCount")
    val VideoCount: Long?,

    @SerializedName("VideoOnDemandCount")
    val VideoOnDemandCount: Long?
)
