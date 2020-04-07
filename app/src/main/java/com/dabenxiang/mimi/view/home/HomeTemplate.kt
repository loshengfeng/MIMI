package com.dabenxiang.mimi.view.home

import com.dabenxiang.mimi.view.adapter.HomeItemType

sealed class HomeTemplate(val type: HomeItemType) {
    data class Header(val iconRes: Int?, val title: String?) : HomeTemplate(HomeItemType.HEADER)
    object Banner : HomeTemplate(HomeItemType.BANNER)
    object Carousel : HomeTemplate(HomeItemType.CAROUSEL)
    object Leaderboard : HomeTemplate(HomeItemType.LEADERBOARD)
    object Recommend : HomeTemplate(HomeItemType.RECOMMEND)
    object Categories : HomeTemplate(HomeItemType.CATEGORIES)
}
