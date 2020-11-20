package com.dabenxiang.mimi.view.club

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.adapter.MemberPostPagedAdapter
import com.dabenxiang.mimi.view.clubdetail.ClubPagerViewHolder

class ClubPagerAdapter: RecyclerView.Adapter<ClubPagerViewHolder>() {

    private val adapterList = arrayListOf<MemberPostPagedAdapter>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClubPagerViewHolder {
        return ClubPagerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_club_pager, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return 2
    }

    override fun onBindViewHolder(holder: ClubPagerViewHolder, position: Int) {
//        val adapter = holder.onBind(position, clubDetailFuncItem, adultListener)
//        adapter?.let { adapterList.add(position, adapter) }
    }
}