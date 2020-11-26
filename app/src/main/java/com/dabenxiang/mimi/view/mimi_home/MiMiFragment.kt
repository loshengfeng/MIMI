package com.dabenxiang.mimi.view.mimi_home

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

    override fun setupFirstTime() {
        super.setupFirstTime()

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

    override fun getLayoutId() = R.layout.fragment_mimi_home

    private fun setupUi(menusItems: List<SecondMenuItem>) {
        viewpager.isSaveEnabled = false
        viewpager.adapter = MiMiViewPagerAdapter(this, menusItems)
        TabLayoutMediator(layout_tab, viewpager) { tab, position ->
            tab.text = menusItems[position].name
        }.attach()
    }

}
