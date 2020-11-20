package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class MenuItem(
    @SerializedName("name")
    val name: String = "",

    @SerializedName("type")
    val type: Int = 0,

    @SerializedName("sorting")
    val sorting: Int = 0,

    @SerializedName("category")
    val category: String = "",

    @SerializedName("menus")
    val menus: ArrayList<MenusItem> = arrayListOf()
)

data class MenusItem(
    @SerializedName("name")
    val name: String = "",

    @SerializedName("type")
    val type: Int = 0,

    @SerializedName("sorting")
    val sorting: Int = 0,

    @SerializedName("category")
    val category: String = ""
)