package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.home.*

class HomeAdapter(val context: Context, private val listener: EventListener) : RecyclerView.Adapter<BaseViewHolder>() {

    interface EventListener {
        fun onHeaderItemClick(view: View, item: HomeTemplate.Header)
        fun onVideoClick(view: View, item: PlayerData)
        fun onLoadAdapter(adapter: HomeCategoriesAdapter, src: HomeTemplate.Categories)
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
                    holder as HeaderViewHolder
                    holder.bind(templateList[position])
                }
                HomeItemType.BANNER.ordinal -> {
                    holder as HomeBannerViewHolder
                    holder.bind(templateList[position])
                }
                HomeItemType.CAROUSEL.ordinal -> {
                    holder as HomeCarouselViewHolder
                    holder.bind(templateList[position])
                }
                HomeItemType.CATEGORIES.ordinal -> {
                    holder as HomeCategoriesViewHolder
                    holder.bind(templateList[position])
                }
                HomeItemType.LEADERBOARD.ordinal -> {
                    holder as HomeLeaderboardViewHolder
                    holder.bind(templateList[position])
                }
                HomeItemType.RECOMMEND.ordinal -> {
                    holder as HomeRecommendViewHolder
                    holder.bind(templateList[position])
                }
                HomeItemType.VIDEOLIST.ordinal -> {
                    holder as HomeVideoListViewHolder
                    holder.bind(templateList[position])
                }
            }
        }
    }
}