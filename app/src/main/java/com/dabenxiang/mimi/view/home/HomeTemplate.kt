package com.dabenxiang.mimi.view.home

import com.dabenxiang.mimi.model.holder.CarouselHolderItem

sealed class HomeTemplate(val type: HomeItemType) {
    data class Header(val iconRes: Int?, val title: String?, val categories: String) : HomeTemplate(HomeItemType.HEADER)
    data class Banner(val imgUrl: String?) : HomeTemplate(HomeItemType.BANNER)
    data class Carousel(val carouselList: List<CarouselHolderItem>) : HomeTemplate(HomeItemType.CAROUSEL)
    object Leaderboard : HomeTemplate(HomeItemType.LEADERBOARD)
    object Recommend : HomeTemplate(HomeItemType.RECOMMEND)
    data class Categories(val title: String?, val categories: String, val isAdult: Boolean) : HomeTemplate(HomeItemType.CATEGORIES)
}
