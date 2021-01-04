package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName
import java.util.*

data class MeItem(
    @SerializedName("id")
    val id: Long,

    @SerializedName("friendlyName")
    val friendlyName: String? = null,

    @SerializedName("availablePoint")
    val availablePoint: Int? = null,

    @SerializedName("hasNewMessage")
    val hasNewMessage: Boolean? = null,

    @SerializedName("promoCode")
    val promoCode: String? = null,

    @SerializedName("isEmailConfirmed")
    val isEmailConfirmed: Boolean? = null,

    @SerializedName("avatarAttachmentId")
    val avatarAttachmentId: Long? = null,

    @SerializedName("isSubscribed")
    val isSubscribed: Boolean,

    @SerializedName("expiryDate")
    val expiryDate: Date? = null,

    @SerializedName("videoCount")
    val videoCount: Int? = null,

    @SerializedName("videoCountLimit")
    val videoCountLimit: Int? = null,

    @SerializedName("videoOnDemandCountLimit")
    val videoOnDemandCountLimit: Int? = null,

    @SerializedName("videoOnDemandCount")
    val videoOnDemandCount: Int? = null,

    @SerializedName("creationDate")
    val creationDate: Date = Date(),

    @SerializedName("isDailyCheckIn")
    val isDailyCheckIn: Boolean,

    @SerializedName("follows")
    val follows: Int? = null,

    @SerializedName("likes")
    val likes: Int? = null,

    @SerializedName("fans")
    val fans: Int? = null,
)

