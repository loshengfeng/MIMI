package com.dabenxiang.mimi.view.my.follow

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.clean.CleanDialogFragment
import com.dabenxiang.mimi.view.dialog.clean.OnCleanDialogListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_my_collection.*
import kotlinx.android.synthetic.main.fragment_my_collection.view.*
import timber.log.Timber

class MyFollowFragment : BaseFragment() {

    companion object {
        const val TAB_FOLLOW_PEOPLE = 0
        const val TAB_FOLLOW_CLUB = 1
    }

    private val viewModel: MyFollowViewModel by viewModels()
    private lateinit var tabLayoutMediator: TabLayoutMediator
    override fun getLayoutId() = R.layout.fragment_my_follow_tab

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("1@@.....")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(getLayoutId(), container, false)

        view.view_pager.adapter = MyFollowViewPagerAdapter(childFragmentManager, lifecycle)
        view.view_pager.offscreenPageLimit = 2
        tabLayoutMediator = TabLayoutMediator(view.tabs, view.view_pager) { tab, position ->
            tab.text = getTabTitle(position)
        }
        tabLayoutMediator.attach()

        view.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.apply {
                    viewModel.lastTabIndex = position
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tool_bar.toolbar_title.text = getString(R.string.follow_title)

        tool_bar.setNavigationOnClickListener {
            navigateTo(NavigateItem.Up)
        }
        tool_bar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_clean -> {
                    Timber.i("onOptionsItemSelected action_clean")
                    deleteAll()
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
            TAB_FOLLOW_PEOPLE -> getString(R.string.follow_people)
            TAB_FOLLOW_CLUB -> getString(R.string.follow_circle)
            else -> null
        }
    }

    private fun deleteAll() {
        CleanDialogFragment.newInstance(
            listener = onCleanDialogListener,
            msgResId = if (viewModel.lastTabIndex == TAB_FOLLOW_PEOPLE)
                R.string.follow_clean_member_dlg_msg else
                R.string.follow_clean_club_dlg_msg
        ).also {
            it.show(
                requireActivity().supportFragmentManager,
                CleanDialogFragment::class.java.simpleName
            )
        }
    }

    private val onCleanDialogListener = object : OnCleanDialogListener {
        override fun onClean() {
            Timber.i("onCleanDialogListener lastTabIndex=${viewModel.lastTabIndex}")
            viewModel.setDeleteNotify()
        }
    }
}