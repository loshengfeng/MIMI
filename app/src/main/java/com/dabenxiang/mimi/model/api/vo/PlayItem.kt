package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class PlayItem(
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
    var like: Boolean?,

    @SerializedName("likeCount")
    var likeCount: Int?,

    @SerializedName("favorite")
    var favorite: Boolean?,

    @SerializedName("favoriteCount")
    var favoriteCount: Int?,

    @SerializedName("commentCount")
    var commentCount: Int?,

    @SerializedName("tags")
    val tags: List<String>?

) : BaseItem()