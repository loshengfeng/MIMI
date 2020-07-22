package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName
import java.util.*

data class PostFavoriteItem(
    @SerializedName("id")
    val id: Long?,

    @SerializedName("postId")
    val postId: Long?,

    @SerializedName("posterId")
    val posterId: Long?,

    @SerializedName("posterName")
    val posterName: String?,

    @SerializedName("posterAvatarAttachmentId")
    val posterAvatarAttachmentId: Long?,

    @SerializedName("postDate")
    val postDate: Date?,

    @SerializedName("type")
    val type: Int?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("content")
    val content: String?,

    @SerializedName("tag")
    val tag: String?,

    @SerializedName("tags")
    val tags: List<String>?,

    @SerializedName("category")
    val category: String?,

    @SerializedName("likeCount")
    var likeCount: Int?,

    @SerializedName("dislikeCount")
    val dislikeCount: Int?,

    @SerializedName("favoriteCount")
    val favoriteCount: Int?,

    @SerializedName("isFollow")
    var isFollow: Boolean?,

    // 0 = like, 1= dislike
    @SerializedName("likeType")
    var likeType: Int?,

    @SerializedName("commentCount")
    val commentCount: Int?

) : BaseItem()