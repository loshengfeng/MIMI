package com.dabenxiang.mimi.model.api.vo


import com.google.gson.annotations.SerializedName

data class TokenRequest(
    @SerializedName("grant_type")
    val grantType: String ="",

    @SerializedName("client_id")
    val clientId: String,

    @SerializedName("client_secret")
    val clientSecret: String
)

data class RefreshTokenRequest(
    @SerializedName("grant_type")
    val grantType: String ="",

    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("client_id")
    val clientId: String,

    @SerializedName("client_secret")
    val clientSecret: String,


)