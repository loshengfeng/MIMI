package com.dabenxiang.mimi.manager.update.data

import com.google.gson.annotations.SerializedName

data class OAutn2TokenItem(
    @SerializedName("access_token")
    val accessToken: String = "",

    @SerializedName("token_type")
    val tokenType: String = "Bearer",

    @SerializedName("expires_in")
    val expires_in: Int = 3600,

    @SerializedName("scope")
    val scope: String = ""

)