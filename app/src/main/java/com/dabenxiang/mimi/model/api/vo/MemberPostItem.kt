package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostStatus
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.VideoConsumeResult
import com.google.gson.annotations.SerializedName
import java.util.*

data class MemberPostItem(

    @SerializedName("id")
    var id: Long = 0,

    @SerializedName("title")
    var title: String = "",

    @SerializedName("cover")
    var cover: String = "",

    @SerializedName("content")
    var content: String = "",

    @SerializedName("type")
    val type: PostType = PostType.TEXT,

    @SerializedName("creationDate")
    val creationDate: Date = Date(),

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
    var tags: ArrayList<String>? = arrayListOf(),

    @SerializedName("clickThroughCount")
    val clickThroughCount: Int = 0,

    @SerializedName("avatarAttachmentId")
    var avatarAttachmentId: Long = 0,

    @SerializedName("creatorId")
    var creatorId: Long = 0,

    @SerializedName("isFollow")
    var isFollow: Boolean = false,

    @SerializedName("reported")
    var reported: Boolean = false,

    @SerializedName("postFriendlyName")
    var postFriendlyName: String = "",

    @SerializedName("status")
    val status: PostStatus? = PostStatus.ONLINE,

    @SerializedName("category")
    val category: String? = "",

    val adItem: AdItem? = null,

    @SerializedName("isFullContent")
    val isFullContent: Boolean = false,

    @SerializedName("deducted")
    val deducted: Boolean = false,

    var videoConsumeType: VideoConsumeResult? = null

) : BaseMemberPostItem()