package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.home.*
import timber.log.Timber

enum class HomeItemType {
    HEADER, BANNER, CAROUSEL, LEADERBOARD, RECOMMEND, CATEGORIES
}

class HomeAdapter(val context: Context, val templateList: List<HomeTemplate>) : RecyclerView.Adapter<HomeViewHolder>() {

    override fun getItemCount(): Int {
        return templateList.count()
    }

    override fun getItemViewType(position: Int): Int {
        val template = templateList[position]
        return template.type.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)

        val viewHolder = when (viewType) {
            HomeItemType.HEADER.ordinal -> {
                HeaderViewHolder(layoutInflater.inflate(R.layout.item_header, parent, false))
            }
            HomeItemType.BANNER.ordinal -> {
                HomeBannerViewHolder(layoutInflater.inflate(R.layout.item_banner, parent, false))
            }
            HomeItemType.CAROUSEL.ordinal -> {
                HomeCarouselViewHolder(layoutInflater.inflate(R.layout.item_carousel, parent, false))
            }
            HomeItemType.CATEGORIES.ordinal -> {
                HomeCategoriesViewHolder(layoutInflater.inflate(R.layout.item_home_categories, parent, false))
            }
            HomeItemType.LEADERBOARD.ordinal -> {
                HomeLeaderboardViewHolder(layoutInflater.inflate(R.layout.item_home_leaderboard, parent, false))
            }
            HomeItemType.RECOMMEND.ordinal -> {
                HomeRecommendViewHolder(layoutInflater.inflate(R.layout.item_home_recommend, parent, false))
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
                val vh = holder as HeaderViewHolder
                val template = templateList[position] as HomeTemplate.Header
                if (template.iconRes != null) {
                    vh.ivIcon.setImageResource(template.iconRes)
                    vh.ivIcon.visibility = View.VISIBLE
                } else {
                    vh.ivIcon.setImageDrawable(null)
                    vh.ivIcon.visibility = View.GONE
                }

                vh.tvTitle.text = template.title
            }
            HomeItemType.BANNER.ordinal -> {
                val vh = holder as HomeBannerViewHolder
            }
            HomeItemType.CAROUSEL.ordinal -> {
                val vh = holder as HomeCarouselViewHolder
                vh.viewPager.adapter = CarouselAdapter()
                vh.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        Timber.d("onPageSelected: $position")
                    }
                })

                vh.pagerIndicator.setViewPager2(vh.viewPager)
            }
            HomeItemType.CATEGORIES.ordinal -> {
                val vh = holder as HomeCategoriesViewHolder
                LinearLayoutManager(context).also { layoutManager ->
                    layoutManager.orientation = LinearLayoutManager.HORIZONTAL
                    vh.recyclerView.layoutManager = layoutManager
                }

                vh.recyclerView.adapter = HomeCategoriesAdapter()
            }
            HomeItemType.LEADERBOARD.ordinal -> {
                val vh = holder as HomeLeaderboardViewHolder
                LinearLayoutManager(context).also { layoutManager ->
                    layoutManager.orientation = LinearLayoutManager.HORIZONTAL
                    vh.recyclerView.layoutManager = layoutManager
                }

                vh.recyclerView.adapter = LeaderboardAdapter()
            }
            HomeItemType.RECOMMEND.ordinal -> {
                val vh = holder as HomeRecommendViewHolder
                GridLayoutManager(context, 2).also { layoutManager ->
                    vh.recyclerView.layoutManager = layoutManager
                }

                vh.recyclerView.adapter = HomeRecommendAdapter()
            }
        }
    }
}