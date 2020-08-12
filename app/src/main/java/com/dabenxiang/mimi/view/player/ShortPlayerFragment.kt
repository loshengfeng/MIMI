package com.dabenxiang.mimi.view.player

import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment

class ShortPlayerFragment : BaseFragment() {

    private val viewModel: ShortPlayerViewModel by viewModels()

    override fun getLayoutId(): Int {
        return R.layout.fragment_short_player
    }

    override fun setupObservers() {
    }

    override fun setupListeners() {
    }
}