package com.dabenxiang.mimi.view.club.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dabenxiang.mimi.model.enums.ClubTabItemType
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_CLIP
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_FOLLOW
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_LATEST
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_NOVEL
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_PICTURE
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_RECOMMEND
import com.dabenxiang.mimi.view.club.pages.ClubItemFragment

class ClubTabAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        TAB_FOLLOW to { ClubItemFragment(ClubTabItemType.FOLLOW) },
        TAB_RECOMMEND to { ClubItemFragment(ClubTabItemType.RECOMMEND) },
        TAB_LATEST to { ClubItemFragment(ClubTabItemType.LATEST) },
        TAB_CLIP to { ClubItemFragment(ClubTabItemType.SHORT_VIDEO) },
        TAB_PICTURE to { ClubItemFragment(ClubTabItemType.PICTURE) },
        TAB_NOVEL to { ClubItemFragment(ClubTabItemType.NOVEL) }
    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}
