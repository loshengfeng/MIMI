package com.dabenxiang.mimi.view.home

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class HomeFragment : BaseFragment() {

    private val viewModel by viewModel<HomeViewModel>()

    override fun getLayoutId() = R.layout.fragment_home

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragment = TestFragment()

        childFragmentManager.beginTransaction().add(R.id.container_home, fragment).commit()

        bottom_navigation.setOnNavigationItemReselectedListener { item ->
            Timber.d("Jeff: $item")
        }

        viewModel.loadHomeCategories()
    }
}