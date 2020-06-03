package com.dabenxiang.mimi.view.splash

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashFragment : BaseFragment<SplashViewModel>() {

    private val viewModel by viewModel<SplashViewModel>()

    override fun fetchViewModel(): SplashViewModel? {
        return viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_splash
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.autoLogin()

        viewModel.isAutoLogin.observe(viewLifecycleOwner, Observer {
            lifecycleScope.launch(Dispatchers.IO) {

                mainViewModel?.loadHomeCategories()

                delay(1000)

                withContext(Dispatchers.Main) {
                    navigateTo(NavigateItem.Destination(R.id.action_splashFragment_to_homeFragment))
                }
            }
        })
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun setupObservers() {

    }

    override fun setupListeners() {
    }
}