package com.dabenxiang.mimi.view.mimi_home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Success
import com.dabenxiang.mimi.model.api.vo.SecondMenusItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_mimi_home.*

class MiMiFragment : BaseFragment() {

    private val viewModel: MiMiViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getMenu()
    }

    override fun getLayoutId() = R.layout.fragment_mimi_home

    override fun setupObservers() {
        viewModel.menusItems.observe(viewLifecycleOwner, {
            when (it) {
                is Success -> setupUi(it.result)
                is Error -> onApiError(it.throwable)
            }
        })
    }

    override fun setupListeners() {

    }

    private fun setupUi(menusItems: List<SecondMenusItem>) {
        viewpager.adapter = MiMiViewPagerAdapter(this, menusItems)
        TabLayoutMediator(layout_tab, viewpager) { tab, position ->
            tab.text = menusItems[position].name
        }.attach()
    }

}
