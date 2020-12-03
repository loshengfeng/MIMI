package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostType
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

data class VideoItem(

    @SerializedName("availablePoint")
    val availablePoint: Long = 0,

    @SerializedName("categories")
    val categories: List<String> = arrayListOf(),

    @SerializedName("commentCount")
    var commentCount: Long = 0,

    @SerializedName("country")
    val country: String = "",

    @SerializedName("cover")
    val cover: String? = "",

    @SerializedName("deducted")
    val deducted: Boolean? = false,

    @SerializedName("description")
    val description: String? = "",

    @SerializedName("favorite")
    var favorite: Boolean = false,

    @SerializedName("favoriteCount")
    var favoriteCount: Long? = 0,

    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("like")
    var like: Boolean? = false,

    @SerializedName("likeType")
    var likeType: LikeType? = null,

    @SerializedName("likeCount")
    var likeCount: Long = 0,

    @SerializedName("dislikeCount")
    var dislikeCount: Long = 0,

    @SerializedName("point")
    val point: Long? = 0,

    @SerializedName("source")
    val source: String? = "",

    @SerializedName("sources")
    val sources: List<Source>? = arrayListOf(),

    @SerializedName("tags")
    val tags: Any? = null,

    @SerializedName("title")
    val title: String? = "",

    @SerializedName("updateTime")
    val updateTime: Date? = Date(),

    @SerializedName("years")
    val years: Long? = 0,

    @SerializedName("reported")
    val reported: Boolean? = false,

    @SerializedName("videoEpisodes")
    val videoEpisodes: List<VideoEpisode>? = arrayListOf(),

    @SerializedName("timesWatched")
    val timesWatched: Int = 0,

    var isAdult: Boolean = false,
    var searchingTag: String = "", // 搜尋的 TAG
    var searchingStr: String = "", // 搜尋的 Name

    var type: PostType? = null,
    val adItem: AdItem? = null
): Serializable

data class Source(
    @SerializedName("name")
    val name: String? = "",

    @SerializedName("videoEpisodes")
    val videoEpisodes: List<VideoEpisode>? = arrayListOf()
)

data class VideoEpisode(
    @SerializedName("episode")
    val episode: String? = "",

    @SerializedName("episodePublishTime")
    val episodePublishTime: String?,

    @SerializedName("id")
    val id: Long? = 0,

    @SerializedName("source")
    val source: String? = "",

    @SerializedName("reported")
    val reported: Boolean?,

    @SerializedName("videoStreams")
    val videoStreams: List<VideoStream>? = arrayListOf()
)

data class VideoStream(
    @SerializedName("episodePublishTime")
    val episodePublishTime: String? = "",

    @SerializedName("id")
    val id: Long? = 0,

    @SerializedName("streamName")
    val streamName: String? = "",

    @SerializedName("sign")
    val sign: String? = "",

    @SerializedName("utcTime")
    val utcTime: Long? = 0,

    @SerializedName("reported")
    var reported: Boolean? = false,
)

data class VideoM3u8Source(
    @SerializedName("id")
    val id: Long? = 0,

    @SerializedName("videoEpisodeId")
    val videoEpisodeId: Long? = 0,

    @SerializedName("streamName")
    val streamName: String? = "",

    @SerializedName("streamUrl")
    val streamUrl: String? = "",

    @SerializedName("isContent")
    val isContent: Boolean = false
)