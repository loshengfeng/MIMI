package com.dabenxiang.mimi.view.home

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.view.ViewPagerIndicator
import kotlinx.android.synthetic.main.item_carousel.view.*
import kotlinx.android.synthetic.main.item_header.view.*
import kotlinx.android.synthetic.main.item_home_leaderboard.view.*
import kotlinx.android.synthetic.main.item_home_recommend.view.*

open class HomeViewHolder(itemView: View) : BaseViewHolder(itemView)

class HeaderViewHolder(itemView: View) : HomeViewHolder(itemView) {
    val ivIcon: ImageView = itemView.iv_icon
    val tvTitle: TextView = itemView.tv_title
    val btnMore: TextView = itemView.btn_more
}

class HomeLeaderboardViewHolder(itemView: View) : HomeViewHolder(itemView) {
    val recyclerView: RecyclerView = itemView.recyclerview_leaderboard
}

class HomeRecommendViewHolder(itemView: View) : HomeViewHolder(itemView) {
    val recyclerView: RecyclerView = itemView.recyclerview_recommend
}

class HomeCarouselViewHolder(itemView: View) : HomeViewHolder(itemView) {
    val viewPager: ViewPager2 = itemView.viewpager
    val pagerIndicator: ViewPagerIndicator = itemView.pager_indicator
}
