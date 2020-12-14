package com.dabenxiang.mimi.model.api.vo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostStatus
import com.dabenxiang.mimi.model.enums.PostType
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "MemberPostItems")
data class MemberPostItem(
    @PrimaryKey
    @SerializedName("id")
    var id: Long = 0,

    @ColumnInfo(name = "title")
    @SerializedName("title")
    var title: String = "",

    @ColumnInfo(name = "cover")
    @SerializedName("cover")
    var cover: String = "",

    @SerializedName("content")
    var content: String = "",

    @ColumnInfo(name = "type")
    @SerializedName("type")
    val type: PostType = PostType.TEXT,

    @ColumnInfo(name = "creationDate")
    @SerializedName("creationDate")
    val creationDate: Date = Date(),

    @ColumnInfo(name = "isFavorite")
    @SerializedName("isFavorite")
    var isFavorite: Boolean = false,

    @ColumnInfo(name = "likeCount")
    @SerializedName("likeCount")
    var likeCount: Int = 0,

    @ColumnInfo(name = "dislikeCount")
    @SerializedName("dislikeCount")
    var dislikeCount: Int = 0,

    @ColumnInfo(name = "likeType")
    @SerializedName("likeType")
    var likeType: LikeType? = null,

    @ColumnInfo(name = "favoriteCount")
    @SerializedName("favoriteCount")
    var favoriteCount: Int = 0,

    @ColumnInfo(name = "commentCount")
    @SerializedName("commentCount")
    var commentCount: Int = 0,

    @ColumnInfo(name = "tags")
    @SerializedName("tags")
    var tags: ArrayList<String>? = arrayListOf(),

    @ColumnInfo(name = "clickThroughCount")
    @SerializedName("clickThroughCount")
    val clickThroughCount: Int = 0,

    @ColumnInfo(name = "avatarAttachmentId")
    @SerializedName("avatarAttachmentId")
    var avatarAttachmentId: Long = 0,

    @ColumnInfo(name = "creatorId")
    @SerializedName("creatorId")
    var creatorId: Long = 0,

    @ColumnInfo(name = "isFollow")
    @SerializedName("isFollow")
    var isFollow: Boolean = false,

    @ColumnInfo(name = "reported")
    @SerializedName("reported")
    var reported: Boolean = false,

    @ColumnInfo(name = "postFriendlyName")
    @SerializedName("postFriendlyName")
    var postFriendlyName: String = "",

    @ColumnInfo(name = "status")
    @SerializedName("status")
    val status: PostStatus? = PostStatus.ONLINE,

    @ColumnInfo(name = "category")
    @SerializedName("category")
    val category: String? = "",

    @ColumnInfo(name = "adItem")
    val adItem: AdItem? = null,

    @ColumnInfo(name = "isFullContent")
    @SerializedName("isFullContent")
    val isFullContent: Boolean = false,

    @ColumnInfo(name = "deducted")
    @SerializedName("deducted")
    var deducted: Boolean = false,

    @ColumnInfo(name = "postId")
    @SerializedName("postId")
    var postId: Long = 0

) : BaseMemberPostItem()