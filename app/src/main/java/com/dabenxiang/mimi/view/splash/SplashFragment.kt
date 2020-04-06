package com.dabenxiang.mimi.view.splash

import android.os.Bundle
import android.os.Handler
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_splash.*
import org.koin.androidx.viewmodel.ext.android.viewModel

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
//        Handler().postDelayed({
//            viewModel.navigateView.postValue(R.id.action_splashFragment_to_loginFragment)
//        }, 5000)
    }

    override fun setupListeners() {
        TODO("Not yet implemented")
    }
}