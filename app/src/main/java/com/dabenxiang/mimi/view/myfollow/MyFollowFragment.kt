package com.dabenxiang.mimi.view.myfollow

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.adapter.ClubFollowAdapter
import com.dabenxiang.mimi.view.adapter.MemberFollowAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_my_follow.*
import kotlinx.android.synthetic.main.fragment_my_follow.item_no_data
import kotlinx.android.synthetic.main.fragment_my_follow.layout_refresh
import kotlinx.android.synthetic.main.fragment_my_follow.rv_content
import kotlinx.android.synthetic.main.fragment_my_follow.tl_type
import kotlinx.android.synthetic.main.item_setting_bar.*
import kotlinx.android.synthetic.main.item_setting_bar.tv_title

class MyFollowFragment : BaseFragment<MyFollowViewModel>() {
    private val viewModel: MyFollowViewModel by viewModels()

    companion object {
        const val NO_DATA = 0
        const val TYPE_MEMBER = 0
        const val TYPE_CLUB = 1
    }

    private val clubFollowAdapter by lazy { ClubFollowAdapter() }

    private val memberFollowAdapter by lazy { MemberFollowAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int { return R.layout.fragment_my_follow }

    override fun fetchViewModel(): MyFollowViewModel? { return viewModel }

    override fun setupObservers() {
        viewModel.clubList.observe(viewLifecycleOwner, Observer {
            refreshUi(it.size)
            rv_content.adapter = clubFollowAdapter
            clubFollowAdapter.submitList(it)
        })

        viewModel.memberList.observe(viewLifecycleOwner, Observer {
            refreshUi(it.size)
            rv_content.adapter = memberFollowAdapter
            memberFollowAdapter.submitList(it)
        })
    }

    override fun setupListeners() {
        View.OnClickListener { btnView ->
            when(btnView.id) {
                R.id.tv_back -> navigateTo(NavigateItem.Up)
                // todo: clean all or ?...
                R.id.tv_clean -> GeneralUtils.showToast(requireContext(), "Clean")
            }
        }.also {
            tv_back.setOnClickListener(it)
            tv_clean.setOnClickListener(it)
        }

        tl_type.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    TYPE_MEMBER -> viewModel.initData(tab.position)
                    TYPE_CLUB -> viewModel.initData(tab.position)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            viewModel.initData(tl_type.selectedTabPosition)
        }
    }

    override fun initSettings() {
        super.initSettings()
        tv_clean.visibility = View.VISIBLE
        tv_title.setText(R.string.follow_title)
        tv_all.text = getString(R.string.follow_all, "0")

        rv_content.adapter = memberFollowAdapter

        viewModel.initData(tl_type.selectedTabPosition)
    }

    private fun refreshUi(size: Int) {
        rv_content.visibility = when(size) {
            NO_DATA -> View.GONE
            else -> View.VISIBLE
        }

        item_no_data.visibility = when(size) {
            NO_DATA -> View.VISIBLE
            else -> View.GONE
        }

        tv_all.text = getString(R.string.follow_all, size.toString())
    }
}
