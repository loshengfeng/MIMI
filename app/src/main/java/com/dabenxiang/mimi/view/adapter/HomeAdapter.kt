package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.home.*
import timber.log.Timber

class HomeAdapter(val context: Context) : RecyclerView.Adapter<HomeViewHolder>() {

    enum class HomeItemType(value: Int) {
        HEADER(0), AD(0), CAROUSEL(1), LEADERBOARD(2), RECOMMEND(3), CATEGORIES(4)
    }

    override fun getItemCount(): Int {
        return 5
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> HomeItemType.CAROUSEL.ordinal
            2 -> HomeItemType.LEADERBOARD.ordinal
            4 -> HomeItemType.RECOMMEND.ordinal
            else -> HomeItemType.HEADER.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)

        val viewHolder = when (viewType) {
            HomeItemType.HEADER.ordinal -> {
                HeaderViewHolder(layoutInflater.inflate(R.layout.item_header, parent, false))
            }
            HomeItemType.AD.ordinal -> {
                HomeViewHolder(layoutInflater.inflate(R.layout.item_header, parent, false))
            }
            HomeItemType.CAROUSEL.ordinal -> {
                HomeCarouselViewHolder(layoutInflater.inflate(R.layout.item_carousel, parent, false))
            }
            HomeItemType.LEADERBOARD.ordinal -> {
                HomeLeaderboardViewHolder(layoutInflater.inflate(R.layout.item_home_leaderboard, parent, false))
            }
            HomeItemType.RECOMMEND.ordinal -> {
                HomeRecommendViewHolder(layoutInflater.inflate(R.layout.item_home_recommend, parent, false))
            }
            HomeItemType.CATEGORIES.ordinal -> {
                HomeViewHolder(layoutInflater.inflate(R.layout.item_header, parent, false))
            }
            else -> {
                HomeViewHolder(layoutInflater.inflate(R.layout.item_header, parent, false))
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {

        when (holder.itemViewType) {
            HomeItemType.HEADER.ordinal -> {
                (holder as HeaderViewHolder).also {
                    if (position == 1) {
                        it.ivIcon.setImageDrawable(context.getDrawable(R.drawable.ico_data))
                        it.tvTitle.text = "熱門"
                    } else if (position == 3) {
                        it.ivIcon.setImageDrawable(context.getDrawable(R.drawable.ico_star))
                        it.tvTitle.text = "推薦"
                    }
                }
            }
            HomeItemType.AD.ordinal -> {

            }
            HomeItemType.CAROUSEL.ordinal -> {
                (holder as HomeCarouselViewHolder).also {
                    it.viewPager.adapter = CarouselAdapter()
                    it.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            Timber.d("onPageSelected: $position")
                        }
                    })

                    it.pagerIndicator.setViewPager2(it.viewPager)
                }
            }
            HomeItemType.LEADERBOARD.ordinal -> {
                (holder as HomeLeaderboardViewHolder).also {
                    LinearLayoutManager(context).also { layoutManager ->
                        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
                        it.recyclerView.layoutManager = layoutManager
                    }

                    it.recyclerView.adapter = LeaderboardAdapter()
                }
            }
            HomeItemType.RECOMMEND.ordinal -> {
                (holder as HomeRecommendViewHolder).also {
                    GridLayoutManager(context, 2).also { layoutManager ->
                        it.recyclerView.layoutManager = layoutManager
                    }

                    it.recyclerView.adapter = HomeRecommendAdapter()
                }
            }
            HomeItemType.CATEGORIES.ordinal -> {

            }
        }
    }
}