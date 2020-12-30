package com.dabenxiang.mimi.model.api.vo

import androidx.room.*
import com.dabenxiang.mimi.model.db.PostDBItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostStatus
import com.dabenxiang.mimi.model.enums.PostType
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(
    tableName = "MemberPostItems"
)
data class MemberPostItem(
    @PrimaryKey
    @ColumnInfo(name = "id")
    @SerializedName("id")
    var id: Long = 0,

    @ColumnInfo(name = "postId")
    @SerializedName("postId")
    var postId: Long = 0,

    @ColumnInfo(name = "title")
    @SerializedName("title")
    var title: String = "",

    @ColumnInfo(name = "cover")
    @SerializedName("cover")
    var cover: String = "",

    @ColumnInfo(name = "postContent")
    @SerializedName("content")
    var postContent: String = "",

    @ColumnInfo(name = "videoDescription")
    val videoDescription: String = "",

    @ColumnInfo(name = "videoCountry")
    val videoCountry: String = "",

    @ColumnInfo(name = "videoSource")
    val videoSource: String = "",

    @ColumnInfo(name = "videoSources")
    val videoSources: ArrayList<Source> = arrayListOf(),

    @ColumnInfo(name = "videoEpisodes")
    val videoEpisodes: ArrayList<VideoEpisode> = arrayListOf(),

    @ColumnInfo(name = "videoTimesWatched")
    val videoTimesWatched: Int = 0,

    @ColumnInfo(name = "videoPerformers")
    val videoPerformers: String = "",

    @ColumnInfo(name = "type")
    @SerializedName("type")
    val type: PostType = PostType.TEXT,

    @ColumnInfo(name = "creationDate")
    @SerializedName("creationDate")
    val creationDate: Date = Date(),

    @ColumnInfo(name = "updateDate")
    val updateDate: Date = Date(),

    @ColumnInfo(name = "avatarAttachmentId")
    @SerializedName("avatarAttachmentId")
    var avatarAttachmentId: Long = 0,

    @ColumnInfo(name = "creatorId")
    @SerializedName("creatorId")
    var creatorId: Long = 0,

    @ColumnInfo(name = "postFriendlyName")
    @SerializedName("postFriendlyName")
    var postFriendlyName: String = "",

    @ColumnInfo(name = "isFollow")
    @SerializedName("isFollow")
    var isFollow: Boolean = false,

    @ColumnInfo(name = "reported")
    @SerializedName("reported")
    var reported: Boolean = false,

    @ColumnInfo(name = "deducted")
    @SerializedName("deducted")
    var deducted: Boolean = false,

    @ColumnInfo(name = "likeType")
    @SerializedName("likeType")
    var likeType: LikeType? = null,

    @ColumnInfo(name = "likeCount")
    @SerializedName("likeCount")
    var likeCount: Int = 0,

    @ColumnInfo(name = "dislikeCount")
    @SerializedName("dislikeCount")
    var dislikeCount: Int = 0,

    @ColumnInfo(name = "isFavorite")
    @SerializedName("isFavorite")
    var isFavorite: Boolean = false,

    @ColumnInfo(name = "favoriteCount")
    @SerializedName("favoriteCount")
    var favoriteCount: Int = 0,

    @ColumnInfo(name = "commentCount")
    @SerializedName("commentCount")
    var commentCount: Int = 0,

    @ColumnInfo(name = "tags")
    @SerializedName("tags")
    var tags: ArrayList<String>? = arrayListOf(),

    @ColumnInfo(name = "status")
    @SerializedName("status")
    val status: PostStatus? = PostStatus.ONLINE,

    @ColumnInfo(name = "adItem")
    var adItem: AdItem? = null,

    ) : BaseMemberPostItem(){

    fun toPlayItem(): PlayItem {
        return PlayItem(
                videoId = id,
                title = title,
                favorite = isFavorite,
                likeCount = likeCount,
                favoriteCount = favoriteCount,
                commentCount = commentCount,
                tags = tags,
                like =  likeType?.value == 0,
                cover = cover,
        )
    }
    }

class MemberPostItemConverters {

    @TypeConverter
    fun itemToJson(item: MemberPostItem): String = Gson().toJson(item)

    @TypeConverter
    fun jsonToItem(value: String):MemberPostItem = Gson().fromJson(value, MemberPostItem::class.java)
}