package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.enums.PostStatus
import com.dabenxiang.mimi.model.enums.PostType
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MemberPostItem(

    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("content")
    val content: String = "",

    @SerializedName("type")
    val type: PostType = PostType.TEXT,

    @SerializedName("creationDate")
    val creationDate: String = "",

    @SerializedName("isFavorite")
    val isFavorite: Boolean = false,

    @SerializedName("likeCount")
    val likeCount: Int = 0,

    @SerializedName("dislikeCount")
    val dislikeCount: Int = 0,

    @SerializedName("favoriteCount")
    val favoriteCount: Int = 0,

    @SerializedName("commentCount")
    val commentCount: Int = 0,

    @SerializedName("tags")
    val tags: ArrayList<String> = arrayListOf(),

    @SerializedName("clickThroughCount")
    val clickThroughCount: Int = 0,

    @SerializedName("avatarAttachmentId")
    val avatarAttachmentId: Long = 0,

    @SerializedName("creatorId")
    val creatorId: Long = 0,

    @SerializedName("isFollow")
    val isFollow: Boolean = false,

    @SerializedName("reported")
    val reported: Boolean = false,

    @SerializedName("status")
    val status: PostStatus = PostStatus.ONLINE
): Serializable