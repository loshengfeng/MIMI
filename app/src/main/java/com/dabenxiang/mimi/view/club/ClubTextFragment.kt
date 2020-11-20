package com.dabenxiang.mimi.view.club

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_club.*
import kotlinx.android.synthetic.main.fragment_order.*
import kotlinx.android.synthetic.main.item_setting_bar.*

class ClubTextFragment : BaseFragment() {

    override fun getLayoutId() = R.layout.fragment_club_text

    override fun setupObservers() {
    }

    override fun setupListeners() {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_title.text = getString(R.string.home_tab_text)

        viewPager.adapter = ClubPagerAdapter()
    }
}