package com.dabenxiang.mimi.view.myfollow

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dabenxiang.mimi.model.enums.MyFollowTabItemType
import com.dabenxiang.mimi.view.myfollow.follow.MyFollowInterestFragment
import com.dabenxiang.mimi.view.myfollow.video.MyFollowItemFragment

class MyFollowViewPagerAdapterV2(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
            MyFollowFragmentV3.TAB_MiMI_VIDEO to { MyFollowItemFragment(MyFollowTabItemType.MIMI_VIDEO) },
            MyFollowFragmentV3.TAB_SMALL_VIDEO to { MyFollowItemFragment(MyFollowTabItemType.SMALL_VIDEO) },
            MyFollowFragmentV3.TAB_POST to {  MyFollowInterestFragment()  },
    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}