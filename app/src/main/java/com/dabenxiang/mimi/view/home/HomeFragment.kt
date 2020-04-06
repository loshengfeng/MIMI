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

        activity?.also { activity ->
            activity.window.statusBarColor = activity.getColor(R.color.color_bar)
        }

        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            Timber.d("Selected: $item")
            true
        }

        //viewModel.loadHomeCategories()
    }
}