package com.dabenxiang.mimi.view.ranking

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.RankingFuncItem
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.adapter.RankingAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_clip.*
import kotlinx.android.synthetic.main.fragment_picture_detail.toolbarContainer
import kotlinx.android.synthetic.main.fragment_ranking.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*

class RankingFragment : BaseFragment() {

    companion object {
        fun createBundle(): Bundle {
            return Bundle().also {
            }
        }
    }

    private val viewModel: RankingViewModel by viewModels()

    private val adapter by  lazy {
        RankingAdapter(requireActivity(),
            RankingFuncItem(
                onItemClick = {},
                getBitmap = { id, position -> viewModel.getBitmap(id= id, position = position) }
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback { navigateTo(NavigateItem.Up) }

        text_toolbar_title.text = getString(R.string.text_ranking)
        toolbarContainer.toolbar.navigationIcon =
            requireContext().getDrawable(R.drawable.btn_back_white_n)
        toolbarContainer.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        rv_ranking_content.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        viewModel.getRanking()
    }


    override fun getLayoutId(): Int {
        return R.layout.fragment_ranking
    }

    override fun setupObservers() {
        viewModel.rankingList.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        viewModel.bitmapResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Loaded -> progressHUD?.dismiss()
                is ApiResult.Success -> rv_ranking_content.adapter?.notifyItemChanged(
                    it.result,
                    RankingAdapter
                )
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })
    }

    override fun setupListeners() {

        tab_temporal_filter.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.setStatisticsTypeFunction(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        tab_type_filter.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.setPostType(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            viewModel.getRanking()
        }

    }
}