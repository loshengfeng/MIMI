package com.dabenxiang.mimi.view.clip

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ClipPagerAdapter(fragment: ClipFragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return ClipPagerFragment()
    }
}