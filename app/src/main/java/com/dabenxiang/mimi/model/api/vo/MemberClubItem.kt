package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.enums.PostType
import com.google.gson.annotations.SerializedName

data class MemberClubItem(

    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("avatarAttachmentId")
    val avatarAttachmentId: Long = 0,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("description")
    val description: String = "",

    @SerializedName("code")
    val code: String = "",

    @SerializedName("followerCount")
    val followerCount: Int = 0,

    @SerializedName("postCount")
    val postCount: Int = 0,

    @SerializedName("posts")
    val posts: ArrayList<PostItem> = arrayListOf(),

    @SerializedName("tag")
    val tag: String = "",

    @SerializedName("isFollow")
    val isFollow: Boolean = false
)

data class PostItem(
    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("type")
    val type: PostType = PostType.TEXT,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("content")
    val content: String = ""
)