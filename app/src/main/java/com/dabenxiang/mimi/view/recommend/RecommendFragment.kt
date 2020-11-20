package com.dabenxiang.mimi.view.recommend

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Success
import com.dabenxiang.mimi.model.api.vo.CategoryBanner
import com.dabenxiang.mimi.view.base.BaseFragment
import com.to.aboomy.pager2banner.IndicatorView
import kotlinx.android.synthetic.main.fragment_recommend.*

class RecommendFragment : BaseFragment() {

    private val viewModel: RecommendViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getBanners()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_recommend
    }

    override fun setupObservers() {
        viewModel.bannerItems.observe(viewLifecycleOwner, {
            when (it) {
                is Success -> setupBannerUi(it.result)
                is Error -> onApiError(it.throwable)
            }
        })
    }

    override fun setupListeners() {

    }

    private fun setupBannerUi(categoryBanners: List<CategoryBanner>) {
        val indicator = IndicatorView(requireContext())
            .setIndicatorColor(Color.LTGRAY)
            .setIndicatorSelectorColor(Color.DKGRAY)

        banner.also {
            it.setIndicator(indicator)
            it.adapter = BannerAdapter(requireContext(), categoryBanners)
        }
    }

}