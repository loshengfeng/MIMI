package com.dabenxiang.mimi.view.splash

import android.os.Bundle
import android.os.Handler
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_splash.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SplashFragment : BaseFragment() {

    private val viewModel by viewModel<SplashViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title.text = "MiMi"
        setupObservers()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_splash
    }

    override fun setupObservers() {
        Timber.d("${SplashFragment::class.java.simpleName}_setupObservers")
    }

    override fun setupListeners() {
        Timber.d("${SplashFragment::class.java.simpleName}_setupListeners")
    }

    override fun initSettings() {
        Timber.d("${SplashFragment::class.java.simpleName}_initSettings")
    }
}