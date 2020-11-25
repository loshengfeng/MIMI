package com.dabenxiang.mimi.view.clip

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dabenxiang.mimi.R

class ClipPagerAdapter(fragment: ClipFragment) :
    FragmentStateAdapter(fragment) {

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ClipPagerViewHolder(
//        LayoutInflater.from(parent.context).inflate(R.layout.item_clip_pager, parent, false)
//    )
//
//    override fun onBindViewHolder(holder: ClipPagerViewHolder, position: Int) =
//        holder.onBind(position, clipFuncItem)

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return ClipPagerFragment()
    }
}