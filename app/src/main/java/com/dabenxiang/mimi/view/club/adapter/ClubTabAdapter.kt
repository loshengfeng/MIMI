package com.dabenxiang.mimi.view.club.adapter

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.club.post.ClubPicFragment
import com.dabenxiang.mimi.view.club.follow.ClubPostFollowFragment
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_CLIP
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_FOLLOW
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_LATEST
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_NOVEL
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_PICTURE
import com.dabenxiang.mimi.view.club.ClubTabFragment.Companion.TAB_RECOMMEND
import com.dabenxiang.mimi.view.club.post.ClubPostTextFragment
import com.dabenxiang.mimi.view.club.latest.ClubLatestFragment

import com.dabenxiang.mimi.view.club.ClubTabViewModel
import kotlinx.android.synthetic.main.fragment_tab_test.*

class ClubTabAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
            
            TAB_FOLLOW to { ClubPostFollowFragment() },

            TAB_RECOMMEND to {
                PageTestFragment.create(
                        fragment.getString(R.string.club_tab_recommend))
            },
            TAB_LATEST to {
                ClubLatestFragment()
            },
            TAB_CLIP to {
                PageTestFragment.create(
                        fragment.getString(R.string.club_tab_clip))
            },
            TAB_PICTURE to { ClubPicFragment() },
            TAB_NOVEL to { ClubPostTextFragment() }
    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}


//Test tab Fragments
private const val KEY_PAGE_NAME = "KEY_PAGE_NAME"
class PageTestFragment : BaseFragment() {

    companion object {
        fun create(name: String) =
                PageTestFragment().apply {
                    arguments = Bundle(1).apply {
                        putString(KEY_PAGE_NAME, name)
                    }
                }
    }

    private val viewModel: ClubTabViewModel by viewModels()

    override fun getLayoutId() = R.layout.fragment_tab_test
    override fun setupObservers() {}
    override fun setupListeners() {}

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tab_name.text = arguments?.getString(KEY_PAGE_NAME, "")
    }
}
