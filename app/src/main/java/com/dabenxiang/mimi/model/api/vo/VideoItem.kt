package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName
import java.util.*

data class VideoItem(
    @SerializedName("availablePoint")
    val availablePoint: Long?,
    @SerializedName("categories")
    val categories: List<String>?,
    @SerializedName("commentCount")
    val commentCount: Long?,
    @SerializedName("country")
    val country: String?,
    @SerializedName("cover")
    val cover: String?,
    @SerializedName("deducted")
    val deducted: Boolean?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("favorite")
    var favorite: Boolean?,
    @SerializedName("favoriteCount")
    var favoriteCount: Long?,
    @SerializedName("id")
    val id: Long?,
    @SerializedName("like")
    var like: Boolean?,
    @SerializedName("likeCount")
    var likeCount: Long?,
    @SerializedName("point")
    val point: Long?,
    @SerializedName("source")
    val source: String?,
    @SerializedName("sources")
    val sources: List<Source>?,
    @SerializedName("tags")
    val tags: Any?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("updateTime")
    val updateTime: Date?,
    @SerializedName("years")
    val years: Long?,

    var isAdult: Boolean = false,
    var searchingTag: String = "", // 搜尋的 TAG
    var searchingStr: String = "" // 搜尋的 Name
)

data class Source(
    @SerializedName("name")
    val name: String?,
    @SerializedName("videoEpisodes")
    val videoEpisodes: List<VideoEpisode>?
)

data class VideoEpisode(
    @SerializedName("episode")
    val episode: String?,
    @SerializedName("id")
    val id: Long?,
    @SerializedName("source")
    val source: String?,
    @SerializedName("videoStreams")
    val videoStreams: List<VideoStream>?
)

data class VideoStream(
    @SerializedName("episodePublishTime")
    val episodePublishTime: String?,
    @SerializedName("id")
    val id: Long?,
    @SerializedName("streamName")
    val streamName: String?
)