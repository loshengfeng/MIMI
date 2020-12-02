package com.dabenxiang.mimi.view.my_pages.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.clean.CleanDialogFragment
import com.dabenxiang.mimi.view.dialog.clean.OnCleanDialogListener
import com.dabenxiang.mimi.view.my_pages.collection.MyCollectionViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_my.*
import kotlinx.android.synthetic.main.fragment_my.view.*

abstract class BaseMyPagesTabFragment: BaseFragment() {

    lateinit var tabLayoutMediator: TabLayoutMediator
    abstract val viewModel: MyPagesViewModel
    val collectionViewModel: MyCollectionViewModel by viewModels({ requireParentFragment() })
    abstract val tabFragmentsCreators: Map<Int, () -> Fragment>
    abstract val viewPagerAdapter:MyViewPagerAdapter

    override fun getLayoutId() = R.layout.fragment_my

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(getLayoutId(), container, false)

        view.view_pager.adapter = viewPagerAdapter
        view.view_pager.offscreenPageLimit = tabFragmentsCreators.size -1
        tabLayoutMediator = TabLayoutMediator(view.tabs, view.view_pager) { tab, position ->
            tab.text = getTabTitle(position)
        }
        tabLayoutMediator.attach()

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
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::tabLayoutMediator.isInitialized) tabLayoutMediator.detach()
    }

    abstract fun setFragmentTitle()
    abstract fun getTabTitle(position: Int): String?


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
            view?.tabs?.let {
                viewModel.setDeleteNotify(it.selectedTabPosition)
            }
        }
    }
}