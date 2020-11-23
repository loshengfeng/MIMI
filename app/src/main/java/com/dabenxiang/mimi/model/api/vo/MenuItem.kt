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
    val menus: List<SubMenuItem> = arrayListOf()
)

data class SubMenuItem(

    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("type")
    val type: LayoutType = LayoutType.GENERAL,

    @SerializedName("sorting")
    val sorting: Int = 0,

    @SerializedName("category")
    val category: String = "",

    @SerializedName("videos")
    val videos: List<RecommendVideoItem> = arrayListOf(),
)

data class RecommendVideoItem(

    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("cover")
    val cover: String = ""
)

