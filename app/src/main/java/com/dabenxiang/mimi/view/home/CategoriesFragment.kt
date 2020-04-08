package com.dabenxiang.mimi.view.home

import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment2
import com.dabenxiang.mimi.view.base.NavigateItem
import kotlinx.android.synthetic.main.fragment_categories.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class CategoriesFragment: BaseFragment2<CategoriesViewModel>() {

    private val viewModel by viewModel<CategoriesViewModel>()

    override fun getLayoutId(): Int {
        return R.layout.fragment_categories
    }

    override fun fetchViewModel(): CategoriesViewModel? {
        return viewModel
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun setupObservers() {

    }

    override fun setupListeners() {
        iv_back.setOnClickListener {
            viewModel.navigateTo(NavigateItem.Up)
        }
    }
}