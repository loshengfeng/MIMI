package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName


data class MeProfileItem(
    @SerializedName("birthday")
    var birthday: String?,

    @SerializedName("email")
    var email: String?,

    @SerializedName("friendlyName")
    var friendlyName: String?,

    @SerializedName("gender")
    var gender: Int?,

    @SerializedName("username")
    var username: String?
)