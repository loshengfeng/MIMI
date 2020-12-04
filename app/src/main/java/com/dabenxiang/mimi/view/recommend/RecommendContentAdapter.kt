package com.dabenxiang.mimi.view.recommend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ThirdMenuItem
import com.dabenxiang.mimi.view.adapter.viewHolder.AdHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.item_recommend.view.*

class RecommendContentAdapter(
    private val thirdMenuItems: List<ThirdMenuItem>,
    private val recommendFuncItem: RecommendFuncItem
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_AD = 0
        const val VIEW_TYPE_VIDEO = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_AD -> {
                val mView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_ad, parent, false)
                AdHolder(mView)
            }

            else -> {
                val mView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_recommend, parent, false)
                RecommendViewHolder(mView)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = thirdMenuItems[position]
        return if (item.adItem != null) VIEW_TYPE_AD
        else VIEW_TYPE_VIDEO
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = thirdMenuItems[position]
        when (holder) {
            is AdHolder -> {
                Glide.with(holder.adImg.context)
                    .load(item.adItem?.href)
                    .into(holder.adImg)

                holder.adImg.setOnClickListener {
                    GeneralUtils.openWebView(holder.adImg.context, item.adItem?.target ?: "")
                }
            }
            is RecommendViewHolder -> {
                holder.titleText.text = item.name
                holder.moreText.setOnClickListener { recommendFuncItem.onMoreClick(item) }
                holder.recommendContentRecycler.also {
                    it.adapter = RecommendVideoAdapter(item.videos, recommendFuncItem)
                    LinearSnapHelper().attachToRecyclerView(it)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return thirdMenuItems.size
    }

    class RecommendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleText: TextView = itemView.tv_title
        var moreText: TextView = itemView.tv_more
        val recommendContentRecycler: RecyclerView = itemView.rv_recommend_content
    }
}