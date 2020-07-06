package com.dabenxiang.mimi.view.home

import com.dabenxiang.mimi.model.enums.HomeItemType

sealed class HomeTemplate(val type: HomeItemType) {

    data class Header(
        val iconRes: Int?,
        val title: String,
        val categories: String
    ) : HomeTemplate(HomeItemType.HEADER)

    data class Statistics(
        val title: String?,
        val categories: String?,
        val isAdult: Boolean
    ) : HomeTemplate(HomeItemType.STATISTICS)

    data class Banner(val imgUrl: String?) : HomeTemplate(HomeItemType.BANNER)
    data class Carousel(val isAdult: Boolean) : HomeTemplate(HomeItemType.CAROUSEL)

    object LeaderBoard : HomeTemplate(HomeItemType.LEADER_BOARD)
    object Recommend : HomeTemplate(HomeItemType.RECOMMEND)
}
