package com.dabenxiang.mimi.view.club.adapter

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_CLIP
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_FOLLOW
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_LATEST
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_NOVEL
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_PICTURE
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_RECOMMEND
import com.dabenxiang.mimi.view.club.ClubTabViewModel
import com.dabenxiang.mimi.view.club.follow.ClubPostFollowFragment
import com.dabenxiang.mimi.view.club.latest.ClubLatestFragment
import com.dabenxiang.mimi.view.club.pic.ClubPostPicFragment
import com.dabenxiang.mimi.view.club.text.ClubPostTextFragment
import com.dabenxiang.mimi.view.club.recommend.ClubRecommendFragment
import com.dabenxiang.mimi.view.club.short.ClubShortVideoFragment
import kotlinx.android.synthetic.main.fragment_tab_test.*
import timber.log.Timber

class ClubTabAdapter(
        context: Context,
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(

            TAB_FOLLOW to { ClubPostFollowFragment() },

            TAB_RECOMMEND to {
                ClubRecommendFragment()
            },
            TAB_LATEST to {
                ClubLatestFragment()
            },
            TAB_CLIP to {
                ClubShortVideoFragment()
            },
            TAB_PICTURE to { ClubPostPicFragment() },
            TAB_NOVEL to { ClubPostTextFragment() }
    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}
