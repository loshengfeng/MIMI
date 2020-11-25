package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.ArrayList

data class ActorVideosItem(

    @SerializedName("id")
    val id: Long = 0L,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("attachmentId")
    val attachmentId: Long = 0L,

    @SerializedName("totalVideo")
    val totalVideo: Long = 0L,

    @SerializedName("totalClick")
    val totalClick: Long = 0L,

    @SerializedName("videos")
    val videos: ArrayList<ActorVideoItem> = arrayListOf()

    ) : Serializable