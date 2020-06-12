package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ProfileItem(
    @SerializedName("username")
    var username: String?,

    @SerializedName("friendlyName")
    var friendlyName: String?,

    @SerializedName("email")
    var email: String?,

    // 0:Female|1:Male
    @SerializedName("gender")
    var gender: Int?,

    @SerializedName("birthday")
    var birthday: String?,

    @SerializedName("id")
    var id: Long?,

    @SerializedName("emailConfirmed")
    var emailConfirmed: Boolean?
) : Serializable