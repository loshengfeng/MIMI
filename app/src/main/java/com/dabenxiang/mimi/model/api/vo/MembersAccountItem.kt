package com.dabenxiang.mimi.model.api.vo
import com.google.gson.annotations.SerializedName

data class MembersAccountItem(
    @SerializedName("email")
    val email: String,

    @SerializedName("friendlyName")
    val friendlyName: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("promoCode")
    val promoCode: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("validationUrl")
    val validationUrl: String
)