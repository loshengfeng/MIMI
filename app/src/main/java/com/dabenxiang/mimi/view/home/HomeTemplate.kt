package com.dabenxiang.mimi.view.home

sealed class HomeTemplate(val type: HomeItemType) {
    data class Header(val iconRes: Int?, val title: String, val categories: String) : HomeTemplate(HomeItemType.HEADER)
    data class Banner(val imgUrl: String?) : HomeTemplate(HomeItemType.BANNER)
    data class Carousel(val isAdult: Boolean) : HomeTemplate(HomeItemType.CAROUSEL)
    object Leaderboard : HomeTemplate(HomeItemType.LEADERBOARD)
    object Recommend : HomeTemplate(HomeItemType.RECOMMEND)
    data class Statistics(val title: String?, val categories: String?, val isAdult: Boolean) : HomeTemplate(HomeItemType.STATISTICS)
}
