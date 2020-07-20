package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostStatus
import com.dabenxiang.mimi.model.enums.PostType
import com.google.gson.annotations.SerializedName
import java.util.*

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
    val creationDate: Date,

    @SerializedName("isFavorite")
    var isFavorite: Boolean = false,

    @SerializedName("likeCount")
    var likeCount: Int = 0,

    @SerializedName("dislikeCount")
    val dislikeCount: Int = 0,

    @SerializedName("likeType")
    var likeType: LikeType = LikeType.DISLIKE,

    @SerializedName("favoriteCount")
    var favoriteCount: Int = 0,

    @SerializedName("commentCount")
    var commentCount: Int = 0,

    @SerializedName("tags")
    val tags: ArrayList<String> = arrayListOf(),

    @SerializedName("clickThroughCount")
    val clickThroughCount: Int = 0,

    @SerializedName("avatarAttachmentId")
    val avatarAttachmentId: Long = 0,

    @SerializedName("creatorId")
    val creatorId: Long = 0,

    @SerializedName("isFollow")
    var isFollow: Boolean = false,

    @SerializedName("reported")
    var reported: Boolean = false,

    @SerializedName("postFriendlyName")
    val postFriendlyName: String = "",

    @SerializedName("status")
    val status: PostStatus = PostStatus.ONLINE

) : BaseMemberPostItem()