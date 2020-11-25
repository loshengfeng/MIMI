package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ActorCategoriesItem(

    @SerializedName("id")
    val id: Long = 0L,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("attachmentId")
    val attachmentId: Long = 0L

    ) : Serializable