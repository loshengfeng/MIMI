package com.dabenxiang.mimi.view.home

import com.dabenxiang.mimi.model.holder.CarouselHolderItem
import com.dabenxiang.mimi.model.holder.VideoHolderItem

sealed class HomeTemplate(val type: HomeItemType) {
    data class Header(val id: String?, val iconRes: Int?, val title: String?) : HomeTemplate(HomeItemType.HEADER)
    data class Banner(val imgUrl: String?) : HomeTemplate(HomeItemType.BANNER)
    data class Carousel(val carouselList: List<CarouselHolderItem>) : HomeTemplate(HomeItemType.CAROUSEL)
    object Leaderboard : HomeTemplate(HomeItemType.LEADERBOARD)
    object Recommend : HomeTemplate(HomeItemType.RECOMMEND)
    data class Categories(val videoList: List<VideoHolderItem>) : HomeTemplate(HomeItemType.CATEGORIES)
    data class VideoList(val videoList: List<VideoHolderItem>) : HomeTemplate(HomeItemType.VIDEOLIST)
}
