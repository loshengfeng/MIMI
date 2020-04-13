package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName


data class MeProfileItem(
    @SerializedName("username")
    var username: String?,

    @SerializedName("friendlyName")
    var friendlyName: String?,

    @SerializedName("email")
    var email: String?,

    @SerializedName("gender")
    var gender: Int?,

    @SerializedName("avatarAttachmentId")
    var avatarAttachmentId: Int?,

    @SerializedName("birthday")
    var birthday: String?,

    @SerializedName("password")
    var password: String?
)