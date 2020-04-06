package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class VideoEpisodeItem(
    @SerializedName("episode")
    val episode: String?,
    @SerializedName("episodePublishTime")
    val episodePublishTime: String?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("videoStreams")
    val videoStreams: List<VideoStream>?
) {
    data class VideoStream(
        @SerializedName("id")
        val id: Int?,
        @SerializedName("streamName")
        val streamName: String?,
        @SerializedName("streamUrl")
        val streamUrl: String?
    )
}