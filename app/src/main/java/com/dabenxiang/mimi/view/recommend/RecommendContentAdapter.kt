package com.dabenxiang.mimi.view.recommend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdClickListener
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.HomeListItem
import com.dabenxiang.mimi.view.adapter.viewHolder.AdHolder
import kotlinx.android.synthetic.main.item_recommend.view.*

class RecommendContentAdapter(
    private val recommendFuncItem: RecommendFuncItem,
    private val adClickListener: AdClickListener
) :
    PagingDataAdapter<HomeListItem, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<HomeListItem>() {
            override fun areItemsTheSame(
                oldItem: HomeListItem,
                newItem: HomeListItem
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: HomeListItem,
                newItem: HomeListItem
            ): Boolean = oldItem == newItem
        }
        const val VIEW_TYPE_AD = 0
        const val VIEW_TYPE_VIDEO = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_AD -> {
                val mView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_ad, parent, false)
                AdHolder(mView, adClickListener)
            }

            else -> {
                val mView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_recommend, parent, false)
                RecommendViewHolder(mView)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (item?.adItem != null) VIEW_TYPE_AD
        else VIEW_TYPE_VIDEO
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is AdHolder -> {
                holder.onBind(item?.adItem?: AdItem())
            }
            is RecommendViewHolder -> {
                holder.titleText.text = item?.name
                holder.moreText.setOnClickListener { item?.run { recommendFuncItem.onMoreClick(this) } }
                holder.recommendContentRecycler.adapter = RecommendVideoAdapter(
                    item?.videos ?: arrayListOf(), recommendFuncItem
                )
            }
        }
    }

    class RecommendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleText: TextView = itemView.tv_title
        var moreText: TextView = itemView.tv_more
        val recommendContentRecycler: RecyclerView = itemView.rv_recommend_content
    }
}