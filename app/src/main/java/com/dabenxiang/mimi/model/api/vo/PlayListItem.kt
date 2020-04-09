package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class PlayListItem(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("subTitle")
    val subTitle: String?,

    @SerializedName("cover")
    val cover: String?,

    @SerializedName("videoId")
    val videoId: Int?,

    @SerializedName("episode")
    val episode: String?,

    @SerializedName("videoEpisodeId")
    val videoEpisodeId: Int?,

    @SerializedName("playlistType")
    val playlistType: Int?
)