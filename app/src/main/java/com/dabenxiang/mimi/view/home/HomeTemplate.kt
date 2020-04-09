package com.dabenxiang.mimi.view.home

sealed class HomeTemplate(val type: HomeItemType) {
    data class Header(val iconRes: Int?, val title: String?) : HomeTemplate(HomeItemType.HEADER)
    object Banner : HomeTemplate(HomeItemType.BANNER)
    object Carousel : HomeTemplate(HomeItemType.CAROUSEL)
    object Leaderboard : HomeTemplate(HomeItemType.LEADERBOARD)
    object Recommend : HomeTemplate(HomeItemType.RECOMMEND)
    class Categories : HomeTemplate(HomeItemType.CATEGORIES)
    object VideoList : HomeTemplate(HomeItemType.VIDEOLIST)
}
