package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ClubFollowItem(
    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("clubId")
    val clubId: Long = 0,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("description")
    val description: String = "",

    @SerializedName("tag")
    val tag: String = "",

    @SerializedName("avatarAttachmentId")
    val avatarAttachmentId: Long = 0,

    @SerializedName("followerCount")
    val followerCount: Int = 0,

    @SerializedName("postCount")
    val postCount: Int = 0
)