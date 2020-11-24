package com.dabenxiang.mimi.view.generalvideo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Success
import com.dabenxiang.mimi.model.api.vo.VideoByCategoryItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val adWidth = pxToDp(requireContext(), getScreenSize(requireActivity()).first)
        val adHeight = (adWidth / 7)

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

        lifecycleScope.launch {
            viewModel.getVideoByCategory(category)
                .collectLatest {
                    generalVideoAdapter.submitData(it)
                }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_search.setOnClickListener {
            // TODO: 跳至搜尋頁面
        }

        tv_filter.setOnClickListener {
            // TODO: 跳至分類頁面
        }

        rv_video.adapter = generalVideoAdapter
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_general_video
    }

    private val onItemClick: (VideoByCategoryItem) -> Unit = {
        // TODO: 跳至播放頁面
        Timber.d("VideoItem Id: ${it.id}")
    }

}