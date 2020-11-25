package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ActorVideoItem(

    @SerializedName("id")
    val id: Long = 0L,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("cover")
    val cover: String = ""

    ) : Serializable