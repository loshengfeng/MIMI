package com.dabenxiang.mimi.view.clubdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.view.adapter.MemberPostPagedAdapter

class ClubPagerAdapter(
    private val clubDetailFuncItem: ClubDetailFuncItem,
    private val adultListener: AdultListener
) : RecyclerView.Adapter<ClubPagerViewHolder>() {

    private val adapterList = arrayListOf<MemberPostPagedAdapter>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClubPagerViewHolder {
        return ClubPagerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_club_pager, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun onBindViewHolder(holder: ClubPagerViewHolder, position: Int) {
        val adapter = holder.onBind(position, clubDetailFuncItem, adultListener)
        adapter?.let { adapterList.add(position, adapter) }
    }

    fun getListAdapter(position: Int): MemberPostPagedAdapter {
        return adapterList[position]
    }

}