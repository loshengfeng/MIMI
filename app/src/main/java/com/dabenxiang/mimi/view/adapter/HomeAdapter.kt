package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.enums.HomeItemType
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.home.*

class HomeAdapter(
    val context: Context,
    private val listener: EventListener,
    private val isAdult: Boolean
) :
    ListAdapter<HomeTemplate, BaseViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<HomeTemplate>() {
                override fun areItemsTheSame(
                    oldItem: HomeTemplate,
                    newItem: HomeTemplate
                ): Boolean {
                    return oldItem.type == newItem.type
                }

                override fun areContentsTheSame(
                    oldItem: HomeTemplate,
                    newItem: HomeTemplate
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }

    interface EventListener {
        fun onHeaderItemClick(view: View, item: HomeTemplate.Header)
        fun onVideoClick(view: View, item: PlayerData)
        fun onLoadStatisticsViewHolder(vh: HomeStatisticsViewHolder, src: HomeTemplate.Statistics)
        fun onLoadCarouselViewHolder(vh: HomeCarouselViewHolder, src: HomeTemplate.Carousel)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (getItem(viewType).type) {
            HomeItemType.HEADER -> {
                HeaderViewHolder(
                    layoutInflater.inflate(R.layout.item_header, parent, false),
                    listener,
                    isAdult
                )
            }
            HomeItemType.BANNER -> {
                HomeBannerViewHolder(
                    layoutInflater.inflate(R.layout.item_banner, parent, false),
                    listener,
                    isAdult
                )
            }
            HomeItemType.CAROUSEL -> {
                HomeCarouselViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_carousel,
                        parent,
                        false
                    ), listener, isAdult
                )
            }
            HomeItemType.STATISTICS -> {
                HomeStatisticsViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_home_statistics,
                        parent,
                        false
                    ), listener, isAdult
                )
            }
            HomeItemType.LEADER_BOARD -> {
                HomeLeaderboardViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_home_leaderboard,
                        parent,
                        false
                    ), listener, isAdult
                )
            }
            HomeItemType.RECOMMEND -> {
                HomeRecommendViewHolder(
                    layoutInflater.inflate(
                        R.layout.item_home_recommend,
                        parent,
                        false
                    ), listener, isAdult
                )
            }
            else -> {
                HeaderViewHolder(
                    layoutInflater.inflate(R.layout.item_header, parent, false),
                    listener,
                    isAdult
                )
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val template = getItem(position)
        when (template.type) {
            HomeItemType.HEADER -> {
                holder as HeaderViewHolder
                holder.bind(template)
            }
            HomeItemType.BANNER -> {
                holder as HomeBannerViewHolder
                holder.bind(template)
            }
            HomeItemType.CAROUSEL -> {
                holder as HomeCarouselViewHolder
                holder.bind(template)
            }
            HomeItemType.STATISTICS -> {
                holder as HomeStatisticsViewHolder
                holder.bind(template)
            }
            HomeItemType.LEADER_BOARD -> {
                holder as HomeLeaderboardViewHolder
                holder.bind(template)
            }
            HomeItemType.RECOMMEND -> {
                holder as HomeRecommendViewHolder
                holder.bind(template)
            }
        }
    }
}
