package com.dabenxiang.mimi.view.club.topic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.view.adapter.MemberPostPagedAdapter

class TopicPagerAdapter(
        private val clubDetailFuncItem: TopicDetailFuncItem,
        private val adultListener: AdultListener
) : RecyclerView.Adapter<TopicPagerViewHolder>() {

    private val adapterList = arrayListOf<MemberPostPagedAdapter>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicPagerViewHolder {
        return TopicPagerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_club_pager, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun onBindViewHolder(holder: TopicPagerViewHolder, position: Int) {
        val adapter = holder.onBind(position, clubDetailFuncItem, adultListener)
        adapter?.let { adapterList.add(position, adapter) }
    }

    fun getListAdapter(position: Int): MemberPostPagedAdapter {
        return adapterList[position]
    }

}