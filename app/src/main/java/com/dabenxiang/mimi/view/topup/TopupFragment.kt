package com.dabenxiang.mimi.view.topup

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.adapter.TopupAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_topup.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class TopupFragment : BaseFragment() {

    private val viewModel by viewModel<TopupViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_topup
    }

    override fun setupObservers() {
        Timber.d("${TopupFragment::class.java.simpleName}_setupObservers")
    }

    override fun setupListeners() {
        Timber.d("${TopupFragment::class.java.simpleName}_setupListeners")
    }

    private fun initSettings() {
        tv_name.text = "好大一棵洋梨"
        tv_coco.text = "200"
        tv_subtitle.text = "副标内容副标内容"
        tv_total.text = "¥ 50.00"

        GridLayoutManager(context, 2).also { layoutManager ->
            rv_content.layoutManager = layoutManager
        }
        rv_content.adapter = TopupAdapter()
    }
}