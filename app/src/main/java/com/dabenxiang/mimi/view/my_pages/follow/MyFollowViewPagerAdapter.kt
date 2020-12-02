package com.dabenxiang.mimi.view.my_pages.follow

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dabenxiang.mimi.view.my_pages.follow.MyFollowFragment.Companion.TAB_FOLLOW_CLUB
import com.dabenxiang.mimi.view.my_pages.follow.MyFollowFragment.Companion.TAB_FOLLOW_PEOPLE
import com.dabenxiang.mimi.view.my_pages.follow.follow_list.MyFollowListFragment

class MyFollowViewPagerAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
            TAB_FOLLOW_PEOPLE to { MyFollowListFragment(TAB_FOLLOW_PEOPLE) },
            TAB_FOLLOW_CLUB to { MyFollowListFragment(TAB_FOLLOW_CLUB) },

    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}