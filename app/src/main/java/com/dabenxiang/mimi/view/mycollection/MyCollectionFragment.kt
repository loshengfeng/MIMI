package com.dabenxiang.mimi.view.mycollection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_my_follow_v3.*
import kotlinx.android.synthetic.main.fragment_my_follow_v3.view.*
import timber.log.Timber

class MyCollectionFragment: BaseFragment() {

    companion object {
        const val TAB_MiMI_VIDEO = 0
        const val TAB_SMALL_VIDEO = 1
        const val TAB_POST = 2
    }

    //    private val viewModel: MyFollowViewModel by viewModels()
    private lateinit var tabLayoutMediator: TabLayoutMediator
    override fun getLayoutId() = R.layout.fragment_my_follow_v3

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(getLayoutId(), container, false)

        view.view_pager.adapter = MyCollectionViewPagerAdapter(childFragmentManager, lifecycle)
        view.view_pager.offscreenPageLimit = 2
        tabLayoutMediator = TabLayoutMediator(view.tabs, view.view_pager) { tab, position ->
            tab.text = getTabTitle(position)
        }
        tabLayoutMediator.attach()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tool_bar.toolbar_title.text = getString(R.string.personal_follow)

        tool_bar.setNavigationOnClickListener {
            navigateTo(NavigateItem.Up)
        }
        tool_bar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_clean -> {
                    Timber.i("onOptionsItemSelected action_clean")
                    //TODO

                }
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.i("ClubTabFragment onResume")

    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("ClubTabFragment onDestroy")
        if (::tabLayoutMediator.isInitialized) tabLayoutMediator.detach()
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