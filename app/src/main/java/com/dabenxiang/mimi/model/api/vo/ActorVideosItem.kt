package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.ArrayList

data class ActorVideosItem(

    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("attachmentId")
    val attachmentId: Int = 0,

    @SerializedName("totalVideo")
    val totalVideo: Int = 0,

    @SerializedName("totalClick")
    val totalClick: Int = 0,

    @SerializedName("videos")
    val videos: ArrayList<ActorVideoItem> = arrayListOf()

    ) : Serializable