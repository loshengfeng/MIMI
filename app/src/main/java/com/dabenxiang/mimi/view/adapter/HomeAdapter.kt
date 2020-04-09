package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.home.*

class HomeAdapter(val context: Context, private val listener: EventListener) : RecyclerView.Adapter<BaseViewHolder>() {

    interface EventListener {
        fun onHeaderItemClick(view: View, template: HomeTemplate.Header)
        fun onVideoClick(view: View)
    }

    private var templateList: List<HomeTemplate>? = null

    fun setDataSrc(src: List<HomeTemplate>) {
        templateList = src
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return templateList?.count() ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        templateList?.also {
            val template = it[position]
            return template.type.ordinal
        }

        return -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)

        val viewHolder = when (viewType) {
            HomeItemType.HEADER.ordinal -> {
                HeaderViewHolder(layoutInflater.inflate(R.layout.item_header, parent, false), listener)
            }
            HomeItemType.BANNER.ordinal -> {
                HomeBannerViewHolder(layoutInflater.inflate(R.layout.item_banner, parent, false), listener)
            }
            HomeItemType.CAROUSEL.ordinal -> {
                HomeCarouselViewHolder(layoutInflater.inflate(R.layout.item_carousel, parent, false), listener)
            }
            HomeItemType.CATEGORIES.ordinal -> {
                HomeCategoriesViewHolder(layoutInflater.inflate(R.layout.item_home_categories, parent, false), listener)
            }
            HomeItemType.LEADERBOARD.ordinal -> {
                HomeLeaderboardViewHolder(layoutInflater.inflate(R.layout.item_home_leaderboard, parent, false), listener)
            }
            HomeItemType.RECOMMEND.ordinal -> {
                HomeRecommendViewHolder(layoutInflater.inflate(R.layout.item_home_recommend, parent, false), listener)
            }
            HomeItemType.VIDEOLIST.ordinal -> {
                HomeVideoListViewHolder(layoutInflater.inflate(R.layout.item_video_list, parent, false), listener)
            }
            else -> {
                HeaderViewHolder(layoutInflater.inflate(R.layout.item_header, parent, false), listener)
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        templateList?.also { templateList ->
            when (holder.itemViewType) {
                HomeItemType.HEADER.ordinal -> {
                    val vh = holder as HeaderViewHolder
                    vh.bind(templateList[position])
                }
                HomeItemType.BANNER.ordinal -> {
                    val vh = holder as HomeBannerViewHolder
                    vh.bind(templateList[position])
                }
                HomeItemType.CAROUSEL.ordinal -> {
                    val vh = holder as HomeCarouselViewHolder
                    vh.bind(templateList[position])
                }
                HomeItemType.CATEGORIES.ordinal -> {
                    val vh = holder as HomeCategoriesViewHolder
                    vh.bind(templateList[position])
                }
                HomeItemType.LEADERBOARD.ordinal -> {
                    val vh = holder as HomeLeaderboardViewHolder
                    vh.bind(templateList[position])
                }
                HomeItemType.RECOMMEND.ordinal -> {
                    val vh = holder as HomeRecommendViewHolder
                    vh.bind(templateList[position])
                }
                HomeItemType.VIDEOLIST.ordinal -> {
                    val vh = holder as HomeVideoListViewHolder
                    vh.bind(templateList[position])
                }
            }
        }
    }
}