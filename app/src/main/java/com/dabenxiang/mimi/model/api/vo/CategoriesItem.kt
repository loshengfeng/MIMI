package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class CategoriesItem(
    @SerializedName("areas")
    val areas: List<String>?,

    @SerializedName("years")
    val years: List<String>?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("categories")
    val categories: List<SecondCategoriesItem>?
)

data class SecondCategoriesItem(
    @SerializedName("name")
    val name: String?,

    @SerializedName("categories")
    val categories: List<SecondCategoriesItem>?
)