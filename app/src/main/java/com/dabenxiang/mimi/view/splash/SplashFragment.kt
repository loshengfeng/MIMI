package com.dabenxiang.mimi.view.splash

import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashFragment : BaseFragment<SplashViewModel>() {

    private val viewModel by viewModel<SplashViewModel>()

    override fun fetchViewModel(): SplashViewModel? {
        return viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_splash
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun setupObservers() {

    }

    override fun setupListeners() {
    }
}