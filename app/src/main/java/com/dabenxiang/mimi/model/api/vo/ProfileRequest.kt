package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ProfileRequest(

    @SerializedName("friendlyName")
    val friendlyName: String?,

    // 0:Female|1:Male
    @SerializedName("gender")
    val gender: Int?,

    @SerializedName("birthday")
    val birthday: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("validationUrl")
    val validationUrl: String?
)