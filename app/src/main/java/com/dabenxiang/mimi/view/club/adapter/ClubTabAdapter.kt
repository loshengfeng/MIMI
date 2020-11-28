package com.dabenxiang.mimi.view.club.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_CLIP
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_FOLLOW
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_LATEST
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_NOVEL
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_PICTURE
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_RECOMMEND
import com.dabenxiang.mimi.view.club.follow.ClubPostFollowFragment
import com.dabenxiang.mimi.view.club.latest.ClubLatestFragment
import com.dabenxiang.mimi.view.club.pic.ClubPostPicFragment
import com.dabenxiang.mimi.view.club.recommend.ClubRecommendFragment
import com.dabenxiang.mimi.view.club.short.ClubShortVideoFragment
import com.dabenxiang.mimi.view.club.text.ClubPostTextFragment

class ClubTabAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
            TAB_FOLLOW to { ClubPostFollowFragment() },
            TAB_RECOMMEND to { ClubRecommendFragment() },
            TAB_LATEST to { ClubLatestFragment() },
            TAB_CLIP to { ClubShortVideoFragment() },
            TAB_PICTURE to { ClubPostPicFragment() },
            TAB_NOVEL to { ClubPostTextFragment() }
    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}
