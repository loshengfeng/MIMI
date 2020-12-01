package com.dabenxiang.mimi.view.mycollection

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dabenxiang.mimi.model.enums.MyFollowTabItemType
import com.dabenxiang.mimi.view.mycollection.favorites.MyCollectionFavoritesFragment
import com.dabenxiang.mimi.view.mycollection.mimi_video.MyCollectionMimiVideoFragment

class MyCollectionViewPagerAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
            MyCollectionFragment.TAB_MiMI_VIDEO to { MyCollectionMimiVideoFragment(MyFollowTabItemType.MIMI_VIDEO) },
            MyCollectionFragment.TAB_SHORT_VIDEO to { MyCollectionMimiVideoFragment(MyFollowTabItemType.SHORT_VIDEO) },
            MyCollectionFragment.TAB_POST to {  MyCollectionFavoritesFragment()  },
    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}