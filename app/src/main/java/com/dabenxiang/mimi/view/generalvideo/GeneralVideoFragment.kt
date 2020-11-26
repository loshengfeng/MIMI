package com.dabenxiang.mimi.view.generalvideo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.generalvideo.GeneralVideoAdapter.Companion.VIEW_TYPE_VIDEO
import com.dabenxiang.mimi.view.generalvideo.paging.VideoLoadStateAdapter
import com.dabenxiang.mimi.widget.utility.GeneralUtils.getScreenSize
import com.dabenxiang.mimi.widget.utility.GeneralUtils.pxToDp
import kotlinx.android.synthetic.main.fragment_general_video.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class GeneralVideoFragment(val category: String) : BaseFragment() {

    private val viewModel: GeneralVideoViewModel by viewModels()

    private val generalVideoAdapter by lazy {
        GeneralVideoAdapter(onItemClick)
    }

    override fun setupFirstTime() {
        super.setupFirstTime()
        viewModel.adWidth = pxToDp(requireContext(), getScreenSize(requireActivity()).first)
        viewModel.adHeight = (viewModel.adWidth / 7)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_search.setOnClickListener {
            // TODO: 跳至搜尋頁面
        }

        tv_filter.setOnClickListener {
            // TODO: 跳至分類頁面
        }

        layout_refresh.setOnRefreshListener {
            generalVideoAdapter.refresh()
        }

        generalVideoAdapter.addLoadStateListener(loadStateListener)

        val loadStateAdapter = VideoLoadStateAdapter(generalVideoAdapter)

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
            .also { it.spanSizeLookup = gridLayoutSpanSizeLookup }

        rv_video.also {
            it.layoutManager = gridLayoutManager
            it.setHasFixedSize(true)
            it.adapter = generalVideoAdapter.withLoadStateFooter(loadStateAdapter)
        }

        lifecycleScope.launch {
            viewModel.getVideoByCategory(category)
                .collectLatest {
                    generalVideoAdapter.submitData(it)
                }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_general_video
    }

    private val onItemClick: (StatisticsItem) -> Unit = {
        // TODO: 跳至播放頁面
        Timber.d("VideoItem Id: ${it.id}")
    }

    private val loadStateListener = { loadStatus: CombinedLoadStates ->
        when (loadStatus.refresh) {
            is LoadState.Error -> {
                Timber.e("Refresh Error: ${(loadStatus.refresh as LoadState.Error).error.localizedMessage}")
                onApiError((loadStatus.refresh as LoadState.Error).error)

                layout_empty_data.visibility = View.VISIBLE
                tv_empty_data.text = getString(R.string.error_video)
                rv_video.visibility = View.INVISIBLE
                if (layout_refresh != null) {
                    layout_refresh.isRefreshing = false
                }
            }
            is LoadState.Loading -> {
                layout_empty_data.visibility = View.VISIBLE
                tv_empty_data.text = getString(R.string.load_video)
                rv_video.visibility = View.INVISIBLE

                if (layout_refresh != null) {
                    layout_refresh.isRefreshing = true
                }
            }
            is LoadState.NotLoading -> {
                if (generalVideoAdapter.isDataEmpty()) {
                    layout_empty_data.visibility = View.VISIBLE
                    tv_empty_data.text = getString(R.string.empty_video)
                    rv_video.visibility = View.INVISIBLE
                } else {
                    layout_empty_data.visibility = View.INVISIBLE
                    rv_video.visibility = View.VISIBLE
                }

                if (layout_refresh != null) {
                    layout_refresh.isRefreshing = false
                }
            }
        }

        when (loadStatus.append) {
            is LoadState.Error -> {
                Timber.e("Append Error:${(loadStatus.append as LoadState.Error).error.localizedMessage}")
                onApiError((loadStatus.refresh as LoadState.Error).error)
            }
            is LoadState.Loading -> {
                Timber.d("Append Loading endOfPaginationReached:${(loadStatus.append as LoadState.Loading).endOfPaginationReached}")
            }
            is LoadState.NotLoading -> {
                Timber.d("Append NotLoading endOfPaginationReached:${(loadStatus.append as LoadState.NotLoading).endOfPaginationReached}")
            }
        }
    }

    private val gridLayoutSpanSizeLookup =
        object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (generalVideoAdapter.getItemViewType(position)) {
                    VIEW_TYPE_VIDEO -> 1
                    else -> 2
                }
            }
        }
}