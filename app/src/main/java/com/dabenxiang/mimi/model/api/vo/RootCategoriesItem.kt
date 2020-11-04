package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RootCategoriesItem(
    @SerializedName("name")
    val name: String,

    @SerializedName("categories")
    val categories: List<CategoriesItem>?
): Serializable {
    fun getNormal() = categories?.get(0)
    fun getAdult() = categories?.get(0)
}

data class CategoriesItem(
    @SerializedName("name")
    val name: String,

    @SerializedName("categories")
    val categories: List<CategoriesItem>?
): Serializable