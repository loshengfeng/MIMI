package com.dabenxiang.mimi.view.recommend

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Success
import com.dabenxiang.mimi.model.api.vo.CategoryBanner
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.category.CategoriesFragment
import com.dabenxiang.mimi.view.player.ui.PlayerV2Fragment
import com.dabenxiang.mimi.view.ranking.RankingFragment
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.to.aboomy.pager2banner.IndicatorView
import com.to.aboomy.pager2banner.ScaleInTransformer
import kotlinx.android.synthetic.main.fragment_recommend.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class RecommendFragment : BaseFragment() {

    companion object {
        private const val INFO_TYPE_LOADING = 0
        private const val INFO_TYPE_EMPTY = 1
        private const val INFO_TYPE_ERROR = 2
    }

    private val viewModel: RecommendViewModel by viewModels()

    private val recommendContentAdapter by lazy {
        val adapter = RecommendContentAdapter(recommendFuncItem, adClickListener)
        val loadStateListener = { loadStatus: CombinedLoadStates ->
            when (loadStatus.refresh) {
                is LoadState.Error -> {
                    Timber.e("refresh Error:${(loadStatus.refresh as LoadState.Error).error.localizedMessage}")
                    showInfoLayout(INFO_TYPE_ERROR)
                }
                is LoadState.Loading -> {
                    Timber.d("refresh Loading endOfPaginationReached:${(loadStatus.refresh as LoadState.Loading).endOfPaginationReached}")
                    showInfoLayout(INFO_TYPE_LOADING)
                }
                is LoadState.NotLoading -> {
                    Timber.d("refresh NotLoading endOfPaginationReached:${(loadStatus.refresh as LoadState.NotLoading).endOfPaginationReached}")
                    takeIf { adapter.itemCount > 0 }?.run { layout_info?.visibility = View.GONE }
                        ?: run { showInfoLayout(INFO_TYPE_EMPTY) }
                }
            }

            when (loadStatus.append) {
                is LoadState.Error -> {
                    Timber.e("append Error:${(loadStatus.append as LoadState.Error).error.localizedMessage}")
                }
                is LoadState.Loading -> {
                    Timber.d("append Loading endOfPaginationReached:${(loadStatus.append as LoadState.Loading).endOfPaginationReached}")
                }
                is LoadState.NotLoading -> {
                    Timber.d("append NotLoading endOfPaginationReached:${(loadStatus.append as LoadState.NotLoading).endOfPaginationReached}")
                }
            }
        }
        adapter.addLoadStateListener(loadStateListener)
        adapter
    }

    override fun setupFirstTime() {
        super.setupFirstTime()

        viewModel.adWidth = GeneralUtils.getAdSize(requireActivity()).first
        viewModel.adHeight = GeneralUtils.getAdSize(requireActivity()).second

        tv_search.text = String.format(
            getString(R.string.text_search_classification),
            getString(R.string.recommend)
        )

        viewModel.bannerItems.observe(this, {
            when (it) {
                is Success -> setupBannerUi(it.result)
                is Error -> onApiError(it.throwable)
                else -> {}
            }
        })

        viewModel.getBanners()

        if (rv_recommend.adapter == null) {
            rv_recommend.adapter = recommendContentAdapter
            getHomeList()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_search.setOnClickListener {
            navToSearch()
        }

        tv_filter.setOnClickListener {
            navToCategory()
        }

        iv_rank.setOnClickListener { navToRanking() }

        btn_retry.setOnClickListener {
            getHomeList()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_recommend
    }

    private fun getHomeList() {
        lifecycleScope.launch {
            viewModel.getHomeList().collectLatest {
                (rv_recommend.adapter as RecommendContentAdapter).submitData(it)
            }
        }
    }

    private fun setupBannerUi(categoryBanners: List<CategoryBanner>) {
        banner.also {
            val indicator = IndicatorView(requireContext())
                .setIndicatorColor(Color.DKGRAY)
                .setIndicatorSelectorColor(Color.LTGRAY)
            it.setIndicator(indicator)
            it.setPageTransformer(ScaleInTransformer())
            it.setPageMargin(
                GeneralUtils.dpToPx(it.context, 20),
                GeneralUtils.dpToPx(it.context, 10)
            )
            it.adapter = BannerAdapter(categoryBanners, bannerFuncItem)
        }
    }

    private val bannerFuncItem by lazy {
        BannerFuncItem(
            { banner -> GeneralUtils.openWebView(requireContext(), banner.url) },
            { id, imageView -> viewModel.loadImage(id, imageView, LoadImageType.PICTURE_THUMBNAIL) }
        )
    }

    private val recommendFuncItem by lazy {
        RecommendFuncItem(
            { videoItem ->
                navToPlayer(PlayerItem(videoItem.id))
            },
            { homeListItem ->
                when (homeListItem.name) {
                    getString(R.string.recommend_today) -> navToRanking()
                    getString(R.string.recommend_new) -> navToCategory()
                    else -> navToCategory(homeListItem.category)
                }
            },
            { source -> viewModel.getDecryptSetting(source) },
            { videoItem, decryptSettingItem, function ->
                viewModel.decryptCover(
                    videoItem,
                    decryptSettingItem,
                    function
                )
            }
        )
    }

    private fun navToRanking() {
        val bundle = RankingFragment.createBundle()
        navigateTo(
            NavigateItem.Destination(
                R.id.action_mimiFragment_to_rankingFragment,
                bundle
            )
        )
    }

    private fun navToCategory(category: String = "") {
        val bundle = CategoriesFragment.createBundle(category = category)
        navigateTo(
            NavigateItem.Destination(
                R.id.action_to_categoriesFragment,
                bundle
            )
        )
    }

    private fun navToSearch() {
        val bundle = SearchVideoFragment.createBundle()
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

    private fun showInfoLayout(type: Int) {
        when(type) {
            INFO_TYPE_LOADING -> {
                tv_info?.text = getString(R.string.load_video)
                btn_retry?.visibility = View.GONE
            }
            INFO_TYPE_EMPTY -> {
                tv_info?.text = getString(R.string.empty_video)
                btn_retry?.visibility = View.GONE
            }
            else -> {
                tv_info?.text = getString(R.string.error_server)
                btn_retry?.visibility = View.VISIBLE
            }
        }
        layout_info?.visibility = View.VISIBLE
    }
}