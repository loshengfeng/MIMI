package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ClubFollowItem(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("clubId")
    val clubId: Int?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("tag")
    val tag: String?,

    @SerializedName("avatarAttachmentId")
    val avatarAttachmentId: Int?,

    @SerializedName("followerCount")
    val followerCount: Int?,

    @SerializedName("postCount")
    val postCount: Int?
)