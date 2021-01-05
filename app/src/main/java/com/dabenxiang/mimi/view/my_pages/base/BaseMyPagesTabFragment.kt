package com.dabenxiang.mimi.view.my_pages.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.clean.CleanDialogFragment
import com.dabenxiang.mimi.view.dialog.clean.OnCleanDialogListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_my.*
import kotlinx.android.synthetic.main.fragment_my.view.*

abstract class BaseMyPagesTabFragment : BaseFragment() {

    lateinit var tabLayoutMediator: TabLayoutMediator
    abstract val viewModel: MyPagesViewModel
    abstract val tabFragmentsCreators: Map<Int, () -> Fragment>

    override fun getLayoutId() = R.layout.fragment_my

    private val dataCountByTab: ArrayList<Int> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(getLayoutId(), container, false)
        view.view_pager.adapter = MyViewPagerAdapter(
            tabFragmentsCreators,
            childFragmentManager,
            lifecycle
        )
        view.view_pager.offscreenPageLimit = tabFragmentsCreators.size - 1
        tabLayoutMediator = TabLayoutMediator(view.tabs, view.view_pager) { tab, position ->
            tab.text = getTabTitle(position)
        }
        tabLayoutMediator.attach()
        onTabSelectedListener?.let { view.tabs.addOnTabSelectedListener(it) }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentTitle()

        tool_bar.setNavigationOnClickListener {
            navigateTo(NavigateItem.Up)
        }
        tool_bar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_clean -> {
                    deleteAll()
                }
            }
            true
        }

        dataCountByTab.clear()
        repeat(tabFragmentsCreators.count()) { dataCountByTab.add(0) }

        viewModel.changeDataCount.observe(viewLifecycleOwner, {
            val tabIndex = it.first
            val count = it.second

            if(tabIndex == view.tabs.selectedTabPosition){
                dataCountByTab[tabIndex] = count
                changeCleanBtnIsEnable(tabIndex)
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        if (::tabLayoutMediator.isInitialized) tabLayoutMediator.detach()
    }

    abstract fun setFragmentTitle()
    abstract fun getTabTitle(position: Int): String?

    open fun deleteAll() {
        CleanDialogFragment.newInstance(onCleanDialogListener).also {
            it.show(
                requireActivity().supportFragmentManager,
                CleanDialogFragment::class.java.simpleName
            )
        }
    }

    open val onCleanDialogListener = object : OnCleanDialogListener {
        override fun onClean() {
            view?.tabs?.let {
                viewModel.setDeleteNotify(it.selectedTabPosition)
            }
        }

        override fun onCancel() {

        }
    }

    open val onTabSelectedListener: TabLayout.OnTabSelectedListener? = null

    fun changeCleanBtnIsEnable(tabIndex: Int) {
//        tool_bar.menu.getItem(0).isEnabled = dataCountByTab[tabIndex] > 0
        //TODO FIX
    }
}