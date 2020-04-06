package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class VideoItem(
    @SerializedName("country")
    val country: String?,

    @SerializedName("cover")
    val cover: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("id")
    val id: Int?,

    @SerializedName("tags")
    val tags: List<String>?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("updateTime")
    val updateTime: String?,

    @SerializedName("videoSources")
    val videoSources: List<VideoSource>?,

    @SerializedName("years")
    val years: Int?
) {

    data class VideoSource(
        @SerializedName("name")
        val name: String?,

        @SerializedName("videoEpisodes")
        val videoEpisodes: List<VideoEpisode>?
    )

    data class VideoEpisode(
        @SerializedName("episode")
        val episode: String?,

        @SerializedName("episodePublishTime")
        val episodePublishTime: String?,

        @SerializedName("id")
        val id: Int?
    )
}