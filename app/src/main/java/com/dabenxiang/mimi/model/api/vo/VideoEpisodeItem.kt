package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class VideoEpisodeItem(
    @SerializedName("episode")
    val episode: String?,
    @SerializedName("episodePublishTime")
    val episodePublishTime: String?,
    @SerializedName("id")
    val id: Long?,
    @SerializedName("reported")
    val reported: Boolean?,
    @SerializedName("videoStreams")
    val videoStreams: List<VideoStream>?
) {
    data class VideoStream(
        @SerializedName("id")
        val id: Long?,
        @SerializedName("sign")
        val sign: String?,
        @SerializedName("streamName")
        val streamName: String?,
        @SerializedName("utcTime")
        val utcTime: Long?,
        @SerializedName("reported")
        val reported: Boolean
    )
}

