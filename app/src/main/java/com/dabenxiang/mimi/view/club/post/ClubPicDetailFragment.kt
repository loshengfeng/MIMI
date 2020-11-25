package com.dabenxiang.mimi.view.club.post

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import kotlinx.android.synthetic.main.fragment_order.*
import kotlinx.android.synthetic.main.item_setting_bar.*

class ClubPicDetailFragment : BaseFragment() {

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun getLayoutId() = R.layout.fragment_club_pic_detail

    override fun setupObservers() {
    }

    override fun setupListeners() {
        tv_back.setOnClickListener {
            navigateTo(NavigateItem.Up)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_title.text = getString(R.string.home_tab_picture)

        viewPager.adapter = ClubPagerAdapter()
    }
}