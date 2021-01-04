package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

class SignInRequest(
        @SerializedName("username")
        val userName: String?,

        @SerializedName("code")
        val code: String?,

        @SerializedName("password")
        val password: String? = ""
)