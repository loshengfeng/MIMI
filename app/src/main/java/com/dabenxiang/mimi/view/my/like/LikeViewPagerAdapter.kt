package com.dabenxiang.mimi.view.my.like

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dabenxiang.mimi.model.enums.MyCollectionTabItemType
import com.dabenxiang.mimi.view.my.collection.favorites.MyCollectionFavoritesFragment
import com.dabenxiang.mimi.view.my.collection.mimi_video.MyCollectionMimiVideoFragment

class LikeViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        LikeFragment.TAB_MiMI_VIDEO to { MyCollectionMimiVideoFragment(MyCollectionTabItemType.MIMI_VIDEO, true) },
        LikeFragment.TAB_POST to {  MyCollectionFavoritesFragment(true)  },
    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}