package com.dabenxiang.mimi.view.mimi_home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Success
import com.dabenxiang.mimi.model.api.vo.SecondMenuItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils.getScreenSize
import com.dabenxiang.mimi.widget.utility.GeneralUtils.pxToDp
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_mimi_home.*

class MiMiFragment : BaseFragment() {

    private val viewModel: MiMiViewModel by viewModels()

    private var viewPagerAdapter: MiMiViewPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.adWidth = pxToDp(requireContext(), getScreenSize(requireActivity()).first)
        viewModel.adHeight = (viewModel.adWidth / 7)

        viewModel.menusItems.observe(this, {
            when (it) {
                is Success -> setupUi(it.result)
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.getMenu()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onPause() {
        viewpager.adapter = null
        super.onPause()
    }

    override fun onResume() {
        viewpager.adapter = viewPagerAdapter
        super.onResume()
    }

    override fun getLayoutId() = R.layout.fragment_mimi_home

    private fun setupUi(menusItems: List<SecondMenuItem>) {
        viewPagerAdapter = MiMiViewPagerAdapter(childFragmentManager, lifecycle, menusItems)
        viewpager.adapter = viewPagerAdapter
        TabLayoutMediator(layout_tab, viewpager) { tab, position ->
            tab.text = menusItems[position].name
        }.attach()
    }

}
