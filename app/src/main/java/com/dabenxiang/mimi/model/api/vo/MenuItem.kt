package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.enums.LayoutType
import com.google.gson.annotations.SerializedName

data class MenuItem(

    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("type")
    val type: Int = 0,

    @SerializedName("sorting")
    val sorting: Int = 0,

    @SerializedName("menus")
    val menus: List<SecondMenuItem> = arrayListOf()
)

data class SecondMenuItem(

    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("parentId")
    val parentId: Long = 0,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("type")
    val type: LayoutType = LayoutType.GENERAL,

    @SerializedName("sorting")
    val sorting: Int = 0,

    @SerializedName("category")
    val category: String = "",
)