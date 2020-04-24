package com.dabenxiang.mimi.model.api.vo


import com.google.gson.annotations.SerializedName

data class TokenItem(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("expires_in")
    val expiresIn: Int,
    @SerializedName("scope")
    val scope: String,
    @SerializedName("token_type")
    val tokenType: String
)