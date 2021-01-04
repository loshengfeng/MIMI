package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class PlayItem(
    @SerializedName("id")
    val id: Long=0,

    @SerializedName("title")
    val title: String?="",

    @SerializedName("description")
    val description: String?="",

    @SerializedName("subTitle")
    val subTitle: String?="",

    @SerializedName("cover")
    val cover: String?="",

    @SerializedName("source")
    val source: String?="",

    @SerializedName("videoId")
    val videoId: Long?=0,

    @SerializedName("episode")
    val episode: String?="",

    @SerializedName("videoEpisodeId")
    val videoEpisodeId: Long?=0,

    @SerializedName("playlistType")
    val playlistType: Long?=0,

    @SerializedName("isAdult")
    val isAdult: Boolean?=true,

    @SerializedName("like")
    var like: Boolean?=true,

    @SerializedName("likeCount")
    var likeCount: Int?=0,

    @SerializedName("favorite")
    var favorite: Boolean?=false,

    @SerializedName("favoriteCount")
    var favoriteCount: Int?=0,

    @SerializedName("commentCount")
    var commentCount: Int?=0,

    @SerializedName("tags")
    val tags: List<String>?=null,

    val adItem: AdItem? = null

) : BaseItem()