package com.dabenxiang.mimi.view.recommend

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Success
import com.dabenxiang.mimi.model.api.vo.CategoryBanner
import com.dabenxiang.mimi.model.api.vo.ThirdMenuItem
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

class RecommendFragment(
    private val thirdMenuItems: List<ThirdMenuItem>
) : BaseFragment() {

    private val viewModel: RecommendViewModel by viewModels()

    private val recommendContentAdapter by lazy {
        RecommendContentAdapter(thirdMenuItems, recommendFuncItem)
    }

    override fun setupFirstTime() {
        super.setupFirstTime()

        tv_search.text = String.format(
            getString(R.string.text_search_classification),
            getString(R.string.recommend)
        )

        viewModel.bannerItems.observe(this, {
            when (it) {
                is Success -> setupBannerUi(it.result)
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.getBanners()
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

        rv_recommend.adapter = recommendContentAdapter
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_recommend
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
            { thirdMenuItem ->
                when (thirdMenuItem.name) {
                    getString(R.string.recommend_today) -> navToRanking()
                    else -> navToCategory(thirdMenuItem.category)
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
}