package com.dabenxiang.mimi.model.api.vo

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
import java.util.*

@Parcelize
data class PostMemberRequest (
    @SerializedName("title")
    val title: String = "",

    @SerializedName("content")
    var content: String = "",

    @SerializedName("type")
    val type: Int = 0,

    @SerializedName("isAdult")
    val isAdult: Boolean = true,

    @SerializedName("tags")
    val tags: ArrayList<String> = arrayListOf()
): Serializable, Parcelable