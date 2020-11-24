package com.dabenxiang.mimi.view.generalvideo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Success
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_general_video.*

class GeneralVideoFragment : BaseFragment() {

    private val viewModel: GeneralVideoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val adWidth = ((GeneralUtils.getScreenSize(requireActivity()).first) * 0.333).toInt()
        val adHeight = (adWidth * 0.142).toInt()

        mainViewModel?.getAdResult?.observe(this, {
            when (it) {
                is Success -> {
                    Glide.with(requireContext())
                        .load(it.result.href)
                        .into(iv_ad)
                    iv_ad.setOnClickListener { _ ->
                        GeneralUtils.openWebView(requireContext(), it.result.target)
                    }
                }
                is Error -> onApiError(it.throwable)
            }

        })

        mainViewModel?.getAd(adWidth, adHeight)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_search.setOnClickListener {
            // TODO: 跳至搜尋頁面
        }

        tv_filter.setOnClickListener {
            // TODO: 跳至分類頁面
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_general_video
    }

}