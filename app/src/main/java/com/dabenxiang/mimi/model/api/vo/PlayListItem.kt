package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class PlayListItem (
    @SerializedName("id")
    val id: Long?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("subTitle")
    val subTitle: String?,

    @SerializedName("cover")
    val cover: String?,

    @SerializedName("source")
    val source: String?,

    @SerializedName("videoId")
    val videoId: Long?,

    @SerializedName("episode")
    val episode: String?,

    @SerializedName("videoEpisodeId")
    val videoEpisodeId: Long?,

    @SerializedName("playlistType")
    val playlistType: Long?,

    @SerializedName("isAdult")
    val isAdult: Boolean?,

    @SerializedName("like")
    val like: Boolean?,

    @SerializedName("likeCount")
    val likeCount: Int?,

    @SerializedName("favorite")
    val favorite: Boolean?,

    @SerializedName("favoriteCount")
    val favoriteCount: Int?,

    @SerializedName("commentCount")
    val commentCount: Int?
)