package com.dabenxiang.mimi.view.myfollow

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_my_follow_v2.view.*
import kotlinx.android.synthetic.main.item_setting_bar.view.*
import timber.log.Timber

class MyFollowFragmentV2 : BaseFragment() {

    companion object {
        const val TAB_MiMI_VIDEO = 0
        const val TAB_SMALL_VIDEO = 1
        const val TAB_POST = 2
    }

    private val viewModel: MyFollowViewModel by viewModels()
    private lateinit var tabLayoutMediator: TabLayoutMediator
    override fun getLayoutId() = R.layout.fragment_my_follow_v2

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(getLayoutId(), container, false)
        view.layout_title.tv_title.text = getString(R.string.personal_follow)
        view.viewpager.adapter = MyFollowViewPagerAdapterV2(childFragmentManager, lifecycle)
        view.viewpager.offscreenPageLimit =7
        tabLayoutMediator = TabLayoutMediator(view.layout_tab,  view.viewpager) { tab, position ->
            tab.text = getTabTitle(position)
        }
        tabLayoutMediator.attach()
        return view
    }

    override fun onResume() {
        super.onResume()
        Timber.i("ClubTabFragment onResume")

    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("ClubTabFragment onDestroy")
        tabLayoutMediator.detach()
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            TAB_MiMI_VIDEO -> getString(R.string.follow_tab_mimi_video)
            TAB_SMALL_VIDEO -> getString(R.string.follow_tab_small_video)
            TAB_POST -> getString(R.string.follow_tab_post)
            else -> null
        }
    }
}
