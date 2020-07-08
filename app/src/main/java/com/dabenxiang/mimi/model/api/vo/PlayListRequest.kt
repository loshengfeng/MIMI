package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class PlayListRequest(
    @SerializedName("videoId")
    val videoId: Long?,

//    @SerializedName("videoEpisodeId")
//    val videoEpisodeId: Int?,

    @SerializedName("playlistType")
    val playlistType: Int?
)