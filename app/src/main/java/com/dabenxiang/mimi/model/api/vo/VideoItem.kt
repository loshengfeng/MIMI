package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class VideoItem(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("cover")
    val cover: String?,

    @SerializedName("tags")
    val tags: List<String>?,

    @SerializedName("country")
    val country: String?,

    @SerializedName("years")
    val years: Int?,

    @SerializedName("updateTime")
    val updateTime: String?,

    @SerializedName("videoSources")
    val videoEpisode: List<VideoEpisode>?

) {

    data class VideoEpisode(
        @SerializedName("id")
        val id: Int?,

        @SerializedName("source")
        val source: String?,

        @SerializedName("episode")
        val episode: String?,

        @SerializedName("videoStreams")
        val videoStreams: VideoStreams?
    )

    data class VideoStreams(
        @SerializedName("id")
        val id: Int?,

        @SerializedName("name")
        val name: String?,

        @SerializedName("episode")
        val episode: String?,

        @SerializedName("episodePublishTime")
        val episodePublishTime: String?
    )
}