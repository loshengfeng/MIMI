package com.dabenxiang.mimi.manager.update.data

import com.google.gson.annotations.SerializedName

data class OAuth2TokenRequest(
    @SerializedName("grant_type")
    val grantType: String = "client_credentials"
)