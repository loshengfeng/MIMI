package com.dabenxiang.mimi.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.home.HomeRecommendViewHolder
import com.dabenxiang.mimi.view.home.HomeViewHolder
import com.dabenxiang.mimi.view.home.HomeLeaderboardViewHolder
import kotlinx.android.synthetic.main.item_home_recommend.view.*
import kotlinx.android.synthetic.main.item_leaderboard.view.*

class HomeAdapter(val context: Context) : RecyclerView.Adapter<HomeViewHolder>() {

    enum class HomeItemType(value: Int) {
        AD(0), BANNER(1), LEADERBOARD(2), RECOMMEND(3), CATEGORIES(4)
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> HomeItemType.LEADERBOARD.ordinal
            1 -> HomeItemType.RECOMMEND.ordinal
            else -> HomeItemType.AD.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)

        val viewHolder = when (viewType) {
            HomeItemType.AD.ordinal -> {
                HomeViewHolder(layoutInflater.inflate(R.layout.item_leaderboard, parent, false))
            }
            HomeItemType.BANNER.ordinal -> {
                HomeViewHolder(layoutInflater.inflate(R.layout.item_leaderboard, parent, false))
            }
            HomeItemType.LEADERBOARD.ordinal -> {
                HomeLeaderboardViewHolder(layoutInflater.inflate(R.layout.item_leaderboard, parent, false))
            }
            HomeItemType.RECOMMEND.ordinal -> {
                HomeRecommendViewHolder(layoutInflater.inflate(R.layout.item_home_recommend, parent, false))
            }
            HomeItemType.CATEGORIES.ordinal -> {
                HomeViewHolder(layoutInflater.inflate(R.layout.item_leaderboard, parent, false))
            }
            else -> {
                HomeViewHolder(layoutInflater.inflate(R.layout.item_leaderboard, parent, false))
            }
        }

        return viewHolder
    }

    override fun getItemCount(): Int {
        return 2
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {

        when (holder.itemViewType) {
            HomeItemType.AD.ordinal -> {

            }
            HomeItemType.BANNER.ordinal -> {

            }
            HomeItemType.LEADERBOARD.ordinal -> {
                LinearLayoutManager(context).also { layoutManager ->
                    layoutManager.orientation = LinearLayoutManager.HORIZONTAL
                    holder.itemView.recyclerview_leaderboard.layoutManager = layoutManager
                }

                holder.itemView.recyclerview_leaderboard.adapter = LeaderboardAdapter()
            }
            HomeItemType.RECOMMEND.ordinal -> {
                GridLayoutManager(context, 2).also { layoutManager ->
                    holder.itemView.recyclerview_recommend.layoutManager = layoutManager
                }

                holder.itemView.recyclerview_recommend.adapter = HomeRecommendAdapter()
            }
            HomeItemType.CATEGORIES.ordinal -> {

            }
        }
    }
}