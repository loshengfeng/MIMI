package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.home.*

class HomeAdapter(val context: Context, private val listener: EventListener, private val isAdult: Boolean) :
    ListAdapter<HomeTemplate, BaseViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<HomeTemplate>() {
                override fun areItemsTheSame(oldItem: HomeTemplate, newItem: HomeTemplate): Boolean {
                    return oldItem.type == newItem.type
                }

                override fun areContentsTheSame(oldItem: HomeTemplate, newItem: HomeTemplate): Boolean {
                    return oldItem == newItem
                }
            }
    }

    interface EventListener {
        fun onHeaderItemClick(view: View, item: HomeTemplate.Header)
        fun onVideoClick(view: View, item: PlayerData)
        fun onLoadAdapter(adapter: HomeCategoriesAdapter, src: HomeTemplate.Categories)
    }

    override fun getItemViewType(position: Int): Int {
        val template = getItem(position)
        return template.type.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)

        val viewHolder = when (viewType) {
            HomeItemType.HEADER.ordinal -> {
                HeaderViewHolder(layoutInflater.inflate(R.layout.item_header, parent, false), listener, isAdult)
            }
            HomeItemType.BANNER.ordinal -> {
                HomeBannerViewHolder(layoutInflater.inflate(R.layout.item_banner, parent, false), listener, isAdult)
            }
            HomeItemType.CAROUSEL.ordinal -> {
                HomeCarouselViewHolder(layoutInflater.inflate(R.layout.item_carousel, parent, false), listener, isAdult)
            }
            HomeItemType.CATEGORIES.ordinal -> {
                HomeCategoriesViewHolder(layoutInflater.inflate(R.layout.item_home_categories, parent, false), listener, isAdult)
            }
            HomeItemType.LEADERBOARD.ordinal -> {
                HomeLeaderboardViewHolder(layoutInflater.inflate(R.layout.item_home_leaderboard, parent, false), listener, isAdult)
            }
            HomeItemType.RECOMMEND.ordinal -> {
                HomeRecommendViewHolder(layoutInflater.inflate(R.layout.item_home_recommend, parent, false), listener, isAdult)
            }
            else -> {
                HeaderViewHolder(layoutInflater.inflate(R.layout.item_header, parent, false), listener, isAdult)
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val template = getItem(position)
        when (holder.itemViewType) {
            HomeItemType.HEADER.ordinal -> {
                holder as HeaderViewHolder
                holder.bind(template)
            }
            HomeItemType.BANNER.ordinal -> {
                holder as HomeBannerViewHolder
                holder.bind(template)
            }
            HomeItemType.CAROUSEL.ordinal -> {
                holder as HomeCarouselViewHolder
                holder.bind(template)
            }
            HomeItemType.CATEGORIES.ordinal -> {
                holder as HomeCategoriesViewHolder
                holder.bind(template)
            }
            HomeItemType.LEADERBOARD.ordinal -> {
                holder as HomeLeaderboardViewHolder
                holder.bind(template)
            }
            HomeItemType.RECOMMEND.ordinal -> {
                holder as HomeRecommendViewHolder
                holder.bind(template)
            }
        }
    }
}