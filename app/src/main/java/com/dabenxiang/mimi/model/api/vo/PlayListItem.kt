package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class PlayListItem(
    @SerializedName("cover")
    val cover: String,

    @SerializedName("episode")
    val episode: String,

    @SerializedName("id")
    val id: Int,

    @SerializedName("playlistType")
    val playlistType: Int,

    @SerializedName("subTitle")
    val subTitle: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("videoEpisodeId")
    val videoEpisodeId: Int,

    @SerializedName("videoId")
    val videoId: Int
)