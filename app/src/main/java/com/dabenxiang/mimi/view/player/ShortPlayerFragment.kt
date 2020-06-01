package com.dabenxiang.mimi.view.player

import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ShortPlayerFragment : BaseFragment<ShortPlayerViewModel>() {

    private val viewModel by viewModel<ShortPlayerViewModel>()

    override fun fetchViewModel(): ShortPlayerViewModel? {
        return viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_short_player
    }

    override fun setupObservers() {
    }

    override fun setupListeners() {
    }
}