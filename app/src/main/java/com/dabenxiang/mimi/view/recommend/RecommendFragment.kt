package com.dabenxiang.mimi.view.recommend

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment

class RecommendFragment : BaseFragment() {

    private val viewModel: RecommendViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_recommend
    }

    override fun setupObservers() {

    }

    override fun setupListeners() {

    }

}