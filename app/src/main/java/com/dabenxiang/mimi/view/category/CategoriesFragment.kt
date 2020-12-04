package com.dabenxiang.mimi.view.category

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Success
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.adapter.FilterTabAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.generalvideo.GeneralVideoAdapter
import com.dabenxiang.mimi.view.pagingfooter.withMimiLoadStateFooter
import com.dabenxiang.mimi.view.player.ui.PlayerV2Fragment
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.view.GridSpaceItemDecoration
import kotlinx.android.synthetic.main.fragment_categories.*
import kotlinx.android.synthetic.main.fragment_categories.layout_empty_data
import kotlinx.android.synthetic.main.fragment_categories.layout_refresh
import kotlinx.android.synthetic.main.fragment_categories.rv_video
import kotlinx.android.synthetic.main.fragment_categories.tv_empty_data
import kotlinx.android.synthetic.main.fragment_general_video.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class CategoriesFragment : BaseFragment() {

    companion object {
        const val KEY_ORDER_BY = "orderByType"
        const val KEY_CATEGORY = "category"
        const val SORT = 0
        const val CATEGORY = 1

        fun createBundle(
            category: String,
            orderByType: Int = StatisticsOrderType.LATEST.value
        ): Bundle {
            return Bundle().also {
                it.putString(KEY_CATEGORY, category)
                it.putInt(KEY_ORDER_BY, orderByType)
            }
        }
    }

    private val viewModel: CategoriesViewModel by viewModels()

    private var orderByType = StatisticsOrderType.LATEST.value
    private var category = ""
    private var lstFilterRV: List<RecyclerView> = listOf()
    private var lstFilterText: ArrayList<List<String>> = arrayListOf()

    private val videoListAdapter by lazy { GeneralVideoAdapter(true, onItemClick) }

    private val onItemClick: (StatisticsItem) -> Unit = {
        navToPlayer(PlayerItem(it.id))
    }

    override fun getLayoutId() = R.layout.fragment_categories

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    private val loadStateListener = { loadStatus: CombinedLoadStates ->
        when (loadStatus.refresh) {
            is LoadState.Error -> {
                Timber.e("Refresh Error: ${(loadStatus.refresh as LoadState.Error).error.localizedMessage}")
                onApiError((loadStatus.refresh as LoadState.Error).error)

                layout_empty_data?.run { this.visibility = View.VISIBLE }
                tv_empty_data?.run { this.text = getString(R.string.error_video) }
                rv_video?.run { this.visibility = View.INVISIBLE }
                layout_refresh?.run { this.isRefreshing = false }
            }
            is LoadState.Loading -> {
                layout_empty_data?.run { this.visibility = View.VISIBLE }
                tv_empty_data?.run { this.text = getString(R.string.load_video) }
                rv_video?.run { this.visibility = View.INVISIBLE }
                layout_refresh?.run { this.isRefreshing = true }
            }
            is LoadState.NotLoading -> {
                if (videoListAdapter.isDataEmpty()) {
                    layout_empty_data?.run { this.visibility = View.VISIBLE }
                    tv_empty_data?.run { this.text = getString(R.string.empty_video) }
                    rv_video?.run { this.visibility = View.INVISIBLE }
                } else {
                    layout_empty_data?.run { this.visibility = View.INVISIBLE }
                    rv_video?.run { this.visibility = View.VISIBLE }
                }

                layout_refresh?.run { this.isRefreshing = false }
            }
        }

        when (loadStatus.append) {
            is LoadState.Error -> {
                Timber.e("Append Error:${(loadStatus.append as LoadState.Error).error.localizedMessage}")
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
                return when (videoListAdapter.getItemViewType(position)) {
                    GeneralVideoAdapter.VIEW_TYPE_VIDEO -> 1
                    else -> 2
                }
            }
        }

    override fun setupFirstTime() {
        super.setupFirstTime()

        rv_video?.run { this.visibility = View.INVISIBLE }

        viewModel.adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        viewModel.adHeight = (viewModel.adWidth * 0.142).toInt()

        category = arguments?.getString(KEY_CATEGORY) ?: ""
        orderByType = arguments?.getInt(KEY_ORDER_BY) ?: StatisticsOrderType.LATEST.value
        setupTitle()

        layout_refresh.setOnRefreshListener {
            videoListAdapter.refresh()
        }

        ib_back.setOnClickListener {
            navigateTo(NavigateItem.Up)
        }

        iv_search.setOnClickListener {
            navToSearch()
        }

        bar_collapsing_filter.setOnClickListener {
            bar_filter.translationY = 0f
            setCollapsingFilterBar(View.GONE)
        }

        tv_all_1.setOnClickListener {
            updateFirstTab(true)
            category = ""
            (lstFilterRV[CATEGORY].adapter as FilterTabAdapter).updateLastSelected(null)
            setupTitle()
            setupCollapsingText()
            getVideos()
        }

        videoListAdapter.addLoadStateListener(loadStateListener)

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
            .also { it.spanSizeLookup = gridLayoutSpanSizeLookup }

        rv_video.also {
            it.layoutManager = gridLayoutManager
            it.setHasFixedSize(true)
            it.adapter = videoListAdapter.withMimiLoadStateFooter { videoListAdapter.retry() }
            it.addItemDecoration(
                GridSpaceItemDecoration(
                    2,
                    GeneralUtils.dpToPx(requireContext(), 10),
                    GeneralUtils.dpToPx(requireContext(), 20),
                    true
                )
            )
            LinearSnapHelper().attachToRecyclerView(rv_video)
        }

        lstFilterRV = listOf(rl_filter_0, rl_filter_1)

        viewModel.showProgress.observe(this, Observer { showProgress ->
            if (showProgress) progressHUD.show()
            else progressHUD.dismiss()
        })

        viewModel.getCategoryResult.observe(this, Observer {
            when (it) {
                is Success -> {
                    tv_all_1.visibility = View.VISIBLE
                    setupFilter(
                        SORT,
                        arrayListOf(
                            getString(R.string.category_newest),
                            getString(R.string.category_top_hit)
                        )
                    )
                    setupFilter(CATEGORY, it.result)
                    setupCollapsingText()
                    adjustContentRV(2)
                    getVideos()
                }
                is Error -> onApiError(it.throwable)
                else -> {
                }
            }
        })

        viewModel.getCategory()
    }

    override fun onResume() {
        super.onResume()
        rv_video.addOnScrollListener(onScrollListener)
    }

    override fun onPause() {
        super.onPause()
        rv_video.removeOnScrollListener(onScrollListener)
    }

    private fun setupFilter(index: Int, list: List<String>) {
        lstFilterText.add(index, list)
        val adapter = FilterTabAdapter(object : FilterTabAdapter.FilterTabAdapterListener {
            override fun onSelectedFilterTab(
                recyclerView: RecyclerView,
                position: Int,
                keyword: String
            ) {
                if (index == CATEGORY) {
                    category = keyword
                    updateFirstTab(false)
                    setupTitle()
                } else {
                    orderByType = if (position == 0) StatisticsOrderType.LATEST.value
                    else StatisticsOrderType.HOTTEST.value
                }
                val adapter = lstFilterRV[index].adapter as FilterTabAdapter
                adapter.notifyDataSetChanged()
                adapter.updateLastSelected(position)

                setupCollapsingText()
                getVideos()
            }
        })
        val initSelectIndex =
            when {
                index == SORT && orderByType == StatisticsOrderType.LATEST.value -> 0
                index == SORT && orderByType == StatisticsOrderType.HOTTEST.value -> 1
                index == CATEGORY && lstFilterText[CATEGORY].contains(category) ->
                    lstFilterText[CATEGORY].indexOf(category)
                else -> null
            }
        adapter.submitList(list, initSelectIndex)
        updateFirstTab(initSelectIndex == null)

        lstFilterRV[index].adapter = adapter
    }

    private fun getVideos() {
        lifecycleScope.launch {
            videoListAdapter.submitData(PagingData.empty())
            viewModel.getVideo(category, orderByType)
                .collectLatest {
                    layout_refresh.isRefreshing = false
                    videoListAdapter.submitData(it)
                }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupCollapsingText() {
        val textSorting =
            if (orderByType == StatisticsOrderType.LATEST.value) getString(R.string.category_newest)
            else getString(R.string.category_top_hit)
        val textCategory =
            if (category.isBlank()) getString(R.string.all)
            else category
        tv_collapsing_filter.text = "$textSorting, $textCategory"
    }

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val filterTranslationY = bar_filter.translationY
            when {
                filterTranslationY > 0 -> bar_filter.translationY = 0f
                filterTranslationY <= -bar_filter.height.toFloat() && dy > 0 -> bar_filter.translationY =
                    -bar_filter.height.toFloat()
                filterTranslationY <= 0 -> {
                    bar_filter.translationY = if (bar_filter.translationY - dy > 0f) {
                        0f
                    } else {
                        bar_filter.translationY - dy
                    }
                }
            }

            when {
                filterTranslationY <= -bar_filter.height.toFloat() && dy > 0 -> setCollapsingFilterBar(
                    View.VISIBLE
                )
                else -> setCollapsingFilterBar(View.GONE)
            }
        }
    }

    private val collapsingFilterAnimator = ObjectAnimator()

    private fun setCollapsingFilterBar(visibility: Int) {
        if (collapsingFilterAnimator.isRunning) {
            return
        } else if (bar_collapsing_filter.visibility == visibility) {
            return
        }
        val start = if (visibility == View.VISIBLE) {
            0f
        } else {
            1f
        }
        val end = if (visibility == View.VISIBLE) {
            1f
        } else {
            0f
        }
        collapsingFilterAnimator.target = bar_collapsing_filter
        collapsingFilterAnimator.setPropertyName("alpha")
        collapsingFilterAnimator.setFloatValues(start, end)
        collapsingFilterAnimator.duration = 500L
        collapsingFilterAnimator.removeAllListeners()
        collapsingFilterAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                if (visibility != View.VISIBLE) {
                    bar_collapsing_filter.visibility = View.GONE
                }
            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {
                if (visibility == View.VISIBLE) {
                    bar_collapsing_filter.visibility = View.VISIBLE
                }
            }
        })
        collapsingFilterAnimator.start()
    }

    private fun updateFirstTab(isSelect: Boolean) {
        takeIf { isSelect }?.also {
            tv_all_1.setTextColor(requireContext().getColor(R.color.color_white_1))
            tv_all_1.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_red_1_radius_6)
        } ?: run {
            tv_all_1.setTextColor(requireContext().getColor(R.color.normal_color_text))
            tv_all_1.background = null
        }
    }

    private fun adjustContentRV(notEmptyCount: Int) {
        rv_video.setPadding(
            0,
            GeneralUtils.dpToPx(requireContext(), 50) * notEmptyCount + GeneralUtils.dpToPx(
                requireContext(),
                15
            ),
            0,
            GeneralUtils.dpToPx(requireContext(), 20)
        )
    }

    private fun setupTitle() {
        tv_category_title.text =
            if (category.isBlank()) getString(R.string.home_tab_video)
            else category
    }

    private fun navToSearch() {
        val bundle = SearchVideoFragment.createBundle(category = category)
        navigateTo(
            NavigateItem.Destination(
                R.id.action_to_searchVideoFragment,
                bundle
            )
        )
    }

    private fun navToPlayer(item: PlayerItem) {
        val bundle = PlayerV2Fragment.createBundle(item)
        navigateTo(
            NavigateItem.Destination(
                R.id.action_to_navigation_player,
                bundle
            )
        )
    }
}