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
    private val tabFragmentsCreators: Map<Int, () -> Fragment>,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}
