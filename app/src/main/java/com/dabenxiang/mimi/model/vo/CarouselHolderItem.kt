package com.dabenxiang.mimi.model.vo

import com.dabenxiang.mimi.model.api.vo.CategoryBanner
import com.dabenxiang.mimi.model.api.vo.StatisticsItem

data class CarouselHolderItem(
    val id: Long?,
    val cover: String? = "",
    val title : String = "",
    val position : Int = 0,
    val url : String = "",
    val content : String = "",
    val sorting : Int = 0,
    val target : Int = 0,
    val intervals : Int = 0,
    val bannerCategory : Int = 0,
    val startTime : String = ""
)

fun List<StatisticsItem>.statisticsItemToCarouselHolderItem(isAdult: Boolean): List<CarouselHolderItem> {
    val result = mutableListOf<CarouselHolderItem>()
    forEach { item ->
        val holderItem = CarouselHolderItem(
            id = item.id,
            cover = item.cover
        )
        result.add(holderItem)
    }
    return result
}

fun List<CategoryBanner>.categoryBannerItemCarouselHolderItem(): List<CarouselHolderItem> {
    val result = mutableListOf<CarouselHolderItem>()
    forEach { item ->
        val holderItem = CarouselHolderItem(
            id = item.id,
            title = item.title,
            position = item.position,
            url = item.url,
            content = item.content,
            sorting = item.sorting,
            target = item.target,
            intervals = item.intervals,
            bannerCategory = item.bannerCategory,
            startTime = item.startTime
        )
        result.add(holderItem)
    }
    return result
}