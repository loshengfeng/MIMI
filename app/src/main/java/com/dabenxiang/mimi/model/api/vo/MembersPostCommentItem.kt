package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName
import java.util.*

data class MembersPostCommentItem(

    @SerializedName("commentCount")
    val commentCount: Long?,

    @SerializedName("content")
    val content: String?,

    @SerializedName("creationDate")
    var creationDate: Date?,

    @SerializedName("creatorId")
    val creatorId: Long,

    @SerializedName("dislikeCount")
    var dislikeCount: Long?,

    @SerializedName("id")
    val id: Long?,

    @SerializedName("likeCount")
    var likeCount: Long?,

    @SerializedName("likeType")
    var likeType: Int?,

    @SerializedName("postAvatarAttachmentId")
    val postAvatarAttachmentId: Long?,

    @SerializedName("postName")
    val postName: String,

    @SerializedName("reported")
    var reported: Boolean?

) : BaseMemberPostItem()