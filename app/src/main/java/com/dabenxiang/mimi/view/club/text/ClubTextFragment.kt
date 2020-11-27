package com.dabenxiang.mimi.view.club.text

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.club.post.ClubPostPagerAdapter
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_club_text.*
import kotlinx.android.synthetic.main.fragment_order.viewPager
import kotlinx.android.synthetic.main.item_setting_bar.*

class ClubTextFragment : BaseFragment() {

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    private var memberPostItem: MemberPostItem? = null

    companion object {
        const val KEY_DATA = "data"
        fun createBundle(item: MemberPostItem): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }
    }

    override fun getLayoutId() = R.layout.fragment_club_text

    override fun setupObservers() {

    }

    override fun setupListeners() {
        tv_back.setOnClickListener {
            navigateTo(NavigateItem.Up)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        memberPostItem = arguments?.getSerializable(PictureDetailFragment.KEY_DATA) as MemberPostItem

        tv_title.text = getString(R.string.home_tab_text)

        viewPager.adapter =
            ClubPostPagerAdapter(
                this,
                memberPostItem!!,
                AdultTabType.TEXT
            )
        viewPager.isSaveEnabled = false

        val title: ArrayList<String> = arrayListOf(getString(R.string.text_detail_title), getString(R.string.comment))

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = title[position]
        }.attach()
    }
}