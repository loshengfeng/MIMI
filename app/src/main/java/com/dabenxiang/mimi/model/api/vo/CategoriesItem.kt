package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class CategoriesItem(
    @SerializedName("id")
    val id: String?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("categories")
    val categories: List<CategoriesItem>?
)