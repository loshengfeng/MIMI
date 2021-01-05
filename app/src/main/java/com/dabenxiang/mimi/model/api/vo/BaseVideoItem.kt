package com.dabenxiang.mimi.model.api.vo

import androidx.room.TypeConverter
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostType
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.io.Serializable
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList

data class VideoItem(

    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("cover")
    val cover: String = "",

    @SerializedName("description")
    val description: String = "",

    @SerializedName("country")
    val country: String = "",

    @SerializedName("updateTime")
    val updateDate: Date = Date(),

    @SerializedName("source")
    val source: String = "",

    @SerializedName("sources")
    val sources: ArrayList<Source>? = arrayListOf(),

    @SerializedName("videoEpisodes")
    val videoEpisodes: ArrayList<VideoEpisode>? = arrayListOf(),

    @SerializedName("reported")
    val reported: Boolean? = false,

    @SerializedName("deducted")
    var deducted: Boolean? = false,

    @SerializedName("like")
    var like: Boolean? = false,

    @SerializedName("likeType")
    var likeType: LikeType? = null,

    @SerializedName("likeCount")
    var likeCount: Int = 0,

    @SerializedName("dislikeCount")
    var dislikeCount: Int = 0,

    @SerializedName("favorite")
    var favorite: Boolean = false,

    @SerializedName("favoriteCount")
    var favoriteCount: Int = 0,

    @SerializedName("commentCount")
    var commentCount: Int = 0,

    @SerializedName("tags")
    val tags: Any? = null,

    @SerializedName("timesWatched")
    val timesWatched: Int = 0,

    @SerializedName("performers")
    val performers: String = "",

    var type: PostType? = null,
    var adItem: AdItem? = null
) : Serializable {
    fun toMemberPostItem(): MemberPostItem {
        val tags = when (tags) {
            is List<*> -> tags as ArrayList<String>
            is String -> {
                tags.split(",").toList() as ArrayList<String>
            }
            else -> arrayListOf()
        }
        return MemberPostItem(
            id = id,
            title = title,
            cover = cover,
            type = type ?: PostType.VIDEO_ON_DEMAND,
            updateDate = updateDate,
            videoDescription = description,
            videoCountry = country,
            videoSource = source,
            videoSources = sources ?: arrayListOf(),
            videoEpisodes = videoEpisodes ?: arrayListOf(),
            videoTimesWatched = timesWatched,
            videoPerformers = performers,
            reported = reported ?: false,
            deducted = deducted ?: false,
            likeType = likeType,
            likeCount = likeCount,
            dislikeCount = dislikeCount,
            isFavorite = favorite,
            favoriteCount = favoriteCount,
            commentCount = commentCount,
            tags = tags,
            adItem = adItem
        )
    }
}

data class Source(
    @SerializedName("name")
    val name: String? = "",

    @SerializedName("videoEpisodes")
    val videoEpisodes: List<VideoEpisode>? = arrayListOf()
)

data class VideoEpisode(
    @SerializedName("episode")
    val episode: String? = "",

    @SerializedName("episodePublishTime")
    val episodePublishTime: String?,

    @SerializedName("id")
    val id: Long? = 0,

    @SerializedName("source")
    val source: String? = "",

    @SerializedName("reported")
    val reported: Boolean?,

    @SerializedName("videoStreams")
    val videoStreams: List<VideoStream>? = arrayListOf()
)

data class VideoStream(
    @SerializedName("episodePublishTime")
    val episodePublishTime: String? = "",

    @SerializedName("id")
    val id: Long? = 0,

    @SerializedName("streamName")
    val streamName: String? = "",

    @SerializedName("sign")
    val sign: String? = "",

    @SerializedName("utcTime")
    val utcTime: Long? = 0,

    @SerializedName("reported")
    var reported: Boolean? = false,
)

data class VideoM3u8Source(
    @SerializedName("id")
    val id: Long? = 0,

    @SerializedName("videoEpisodeId")
    val videoEpisodeId: Long? = 0,

    @SerializedName("streamName")
    val streamName: String? = "",

    @SerializedName("streamUrl")
    val streamUrl: String? = "",

    @SerializedName("isContent")
    val isContent: Boolean = false
)

class SourceArrayListConverters {
    @TypeConverter
    fun fromString(value: String): ArrayList<Source> {
        val listType: Type = object : TypeToken<ArrayList<Source>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<Source>): String {
        return Gson().toJson(list)
    }
}

class VideoEpisodeArrayListConverters {
    @TypeConverter
    fun fromString(value: String): ArrayList<VideoEpisode> {
        val listType: Type = object : TypeToken<ArrayList<VideoEpisode>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<VideoEpisode>): String {
        return Gson().toJson(list)
    }
}
