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
    val favorite: Boolean?,
    @SerializedName("favoriteCount")
    val favoriteCount: Long?,
    @SerializedName("id")
    val id: Long?,
    @SerializedName("like")
    val like: Boolean?,
    @SerializedName("likeCount")
    val likeCount: Long?,
    @SerializedName("point")
    val point: Long?,
    @SerializedName("source")
    val source: String?,
    @SerializedName("sources")
    val sources: List<Source>?,
    @SerializedName("tags")
    val tags: List<String>?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("updateTime")
    val updateTime: Date?,
    @SerializedName("years")
    val years: Long?
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