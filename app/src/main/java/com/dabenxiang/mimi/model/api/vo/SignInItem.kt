package com.dabenxiang.mimi.model.api.vo


import com.google.gson.annotations.SerializedName

data class SignInItem(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("expires_in")
    val expiresIn: Int,

    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("token_type")
    val tokenType: String
)