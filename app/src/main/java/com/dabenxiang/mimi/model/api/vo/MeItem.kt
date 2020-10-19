package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName
import java.util.*

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
    val avatarAttachmentId: Long?,

    @SerializedName("isSubscribed")
    val isSubscribed: Boolean,

    @SerializedName("expiryDate")
    val expiryDate: Date = Date(),

    @SerializedName("videoCount")
    val videoCount: Int?,

    @SerializedName("videoOnDemandCount")
    val videoOnDemandCount: Int?,

    @SerializedName("creationDate")
    val creationDate: Date = Date(),
)
