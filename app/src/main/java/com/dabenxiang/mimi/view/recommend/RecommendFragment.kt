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
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.ranking.RankingFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.to.aboomy.pager2banner.IndicatorView
import kotlinx.android.synthetic.main.fragment_recommend.*
import timber.log.Timber

class RecommendFragment(
    private val thirdMenuItems: List<ThirdMenuItem>
) : BaseFragment() {

    private val viewModel: RecommendViewModel by viewModels()

    private val recommendContentAdapter by lazy {
        RecommendContentAdapter(thirdMenuItems, recommendFuncItem)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            // TODO: 跳至搜尋頁面
        }

        tv_filter.setOnClickListener {
            // TODO: 跳至分類頁面
        }

        iv_rank.setOnClickListener {
            val bundle = RankingFragment.createBundle()
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_mimiFragment_to_rankingFragment,
                    bundle
                )
            )
        }

        rv_recommend.adapter = recommendContentAdapter
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_recommend
    }

    private fun setupBannerUi(categoryBanners: List<CategoryBanner>) {
        val indicator = IndicatorView(requireContext())
            .setIndicatorColor(Color.LTGRAY)
            .setIndicatorSelectorColor(Color.DKGRAY)

        banner.also {
            it.setIndicator(indicator)
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
            { videoItem -> Timber.d("VideoItem Id: ${videoItem.id}") },
            { Timber.d("OnMore Click...") }
        )
    }
}