package com.dabenxiang.mimi.view.my.like

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
import com.dabenxiang.mimi.view.my.collection.MyCollectionFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_my_collection.*
import kotlinx.android.synthetic.main.fragment_my_collection.view.*
import timber.log.Timber


class LikeFragment : BaseFragment() {
    private val viewModel: LikeViewModel by viewModels()

    companion object {
        const val TAB_MiMI_VIDEO = 0
        const val TAB_POST = 1
    }

    private lateinit var tabLayoutMediator: TabLayoutMediator

    override fun getLayoutId() = R.layout.fragment_my_collection

    private var vpAdapter: LikeViewPagerAdapter? = null

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(getLayoutId(), container, false)

        view.view_pager.adapter = LikeViewPagerAdapter(childFragmentManager, lifecycle)
        view.view_pager.offscreenPageLimit = 2
        tabLayoutMediator = TabLayoutMediator(view.tabs, view.view_pager) { tab, position ->
            tab.text = getTabTitle(position)
        }
        tabLayoutMediator.attach()

        view.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.apply{
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

        tool_bar.toolbar_title.text = getString(R.string.like_title)

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

    override fun onDestroy() {
        super.onDestroy()
        if (::tabLayoutMediator.isInitialized) tabLayoutMediator.detach()
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            TAB_MiMI_VIDEO -> getString(R.string.follow_tab_mimi_video)
            TAB_POST -> getString(R.string.follow_tab_post)
            else -> null
        }
    }

    private fun deleteAll(){
        CleanDialogFragment.newInstance(onCleanDialogListener).also {
            it.show(
                requireActivity().supportFragmentManager,
                CleanDialogFragment::class.java.simpleName
            )
        }
    }

    private val onCleanDialogListener = object : OnCleanDialogListener {
        override fun onClean() {

            when(viewModel.lastTabIndex){
                MyCollectionFragment.TAB_MiMI_VIDEO ->{
                    //TODO
                }
                MyCollectionFragment.TAB_SHORT_VIDEO ->{
                    //TODO
                }
                else ->{
                    //TODO
                }
            }
        }
    }
}
