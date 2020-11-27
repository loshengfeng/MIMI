package com.dabenxiang.mimi.view.clip

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dabenxiang.mimi.model.enums.OrderType
import com.dabenxiang.mimi.model.enums.StatisticsOrderType

class ClipPagerAdapter(fragment: ClipFragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        val orderType = when(position) {
            0 -> StatisticsOrderType.LATEST
            else -> StatisticsOrderType.HOTTEST
        }
        return ClipPagerFragment(orderType)
    }
}