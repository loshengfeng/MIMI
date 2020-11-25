package com.dabenxiang.mimi.model.vo

import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.SimpleVideoItem
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.model.api.vo.VideoItem

sealed class BaseVideoItem {
    data class Banner(val adItem: AdItem) : BaseVideoItem()
    data class Video(
        val id: Long?,
        val title: String?,
        val resolution: String?,
        val info: String?,
        val imgUrl: String?,
        val isAdult: Boolean,
        val tags: List<String>?
    ) : BaseVideoItem()
}

fun List<VideoItem>.searchItemToVideoItem(isAdult: Boolean): List<BaseVideoItem.Video> {
    val result = mutableListOf<BaseVideoItem.Video>()
    forEach { item ->
        val holderItem = BaseVideoItem.Video(
            id = item.id,
            title = item.title,
            imgUrl = item.cover,
            isAdult = isAdult,
            resolution = "",
            info = "",
            tags = item.tags as List<String>
        )
        result.add(holderItem)
    }
    return result
}

fun List<StatisticsItem>.statisticsItemToVideoItem(isAdult: Boolean): List<BaseVideoItem.Video> {
    val result = mutableListOf<BaseVideoItem.Video>()
    forEach { item ->
        val holderItem = BaseVideoItem.Video(
            id = item.id,
            title = item.title,
            imgUrl = item.cover,
            isAdult = isAdult,
            resolution = "",
            info = "",
            tags = (item.tags as List<String>)
        )
        result.add(holderItem)
    }
    return result
}

fun List<SimpleVideoItem>.simpleVideoItemToVideoItem(isAdult: Boolean): List<BaseVideoItem.Video> {
    val result = mutableListOf<BaseVideoItem.Video>()
    forEach { item ->
        val holderItem = BaseVideoItem.Video(
            id = item.id,
            title = item.title,
            imgUrl = item.cover,
            isAdult = isAdult,
            resolution = "",
            info = "",
            tags = ArrayList<String>()
        )
        result.add(holderItem)
    }
    return result
}