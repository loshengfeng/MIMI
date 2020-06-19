package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName
import java.util.*

data class MembersPostCommentItem(
    @SerializedName("commentCount")
    val commentCount: Long?,
    @SerializedName("content")
    val content: String?,
    @SerializedName("creationDate")
    val creationDate: Date?,
    @SerializedName("creatorId")
    val creatorId: Long?,
    @SerializedName("dislikeCount")
    val dislikeCount: Long?,
    @SerializedName("id")
    val id: Long?,
    @SerializedName("likeCount")
    val likeCount: Long?,
    @SerializedName("likeType")
    val likeType: Int?,
    @SerializedName("postAvatarAttachmentId")
    val postAvatarAttachmentId: Long?,
    @SerializedName("postName")
    val postName: String?,
    @SerializedName("reported")
    val reported: Boolean?
)