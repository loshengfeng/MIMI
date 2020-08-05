package com.dabenxiang.mimi.model.vo

import com.dabenxiang.mimi.model.api.vo.StatisticsItem

data class CarouselHolderItem(
    val id: Long?,
    val cover: String?
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