package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName
import java.util.*

class ActorsItem(
    @SerializedName("actorVideos")
    val actorVideos: ArrayList<ActorVideosItem> = arrayListOf(),

    @SerializedName("categories")
    val actorCategories: ArrayList<ActorCategoriesItem> = arrayListOf()
)