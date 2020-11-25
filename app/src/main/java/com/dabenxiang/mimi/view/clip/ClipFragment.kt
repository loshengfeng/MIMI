package com.dabenxiang.mimi.view.clip

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_clip.*

class ClipFragment: BaseFragment() {

    companion object {
        val tabTitle = arrayListOf(
            App.self.getString(R.string.clip_newest),
            App.self.getString(R.string.clip_top_hit)
        )

        fun createBundle(
            items: ArrayList<MemberPostItem>,
            position: Int,
            showComment: Boolean = false
        ): Bundle {
            return Bundle()
        }
    }

    private val clipPagerAdapter by lazy {
        ClipPagerAdapter(this)
    }

    override fun getLayoutId() = R.layout.fragment_clip

    override val bottomNavigationVisibility = View.VISIBLE

    override val isNavTransparent: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.run {
            this.statusBarColor = ContextCompat.getColor(
                requireContext(),
                R.color.color_black_1
            )
        }
    }

    override fun setupFirstTime() {
        viewPager.isSaveEnabled = false
        viewPager.adapter = clipPagerAdapter
        TabLayoutMediator(tl_type, viewPager) { tab, position ->
            tab.text = tabTitle[position]
            viewPager.setCurrentItem(tab.position, true)
        }.attach()
    }
}