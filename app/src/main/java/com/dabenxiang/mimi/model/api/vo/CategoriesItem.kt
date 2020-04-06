package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

//TODO: 不確定
data class CategoriesItem (
    @SerializedName("content")
    val content: Content?,

    @SerializedName("code")
    val code: Long,

    @SerializedName("message")
    val message: String
) {

    data class Content (
        @SerializedName("id")
        val id: String?,

        @SerializedName("name")
        val name: String?,

        @SerializedName("categories")
        val categories: List<Content>?
    )
}