package com.dabenxiang.mimi.view.post

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_edit_video.*
import kotlinx.android.synthetic.main.item_setting_bar.*


class EditVideoFragment : BaseFragment() {

    private lateinit var editVideoFragmentPagerAdapter: EditVideoFragmentPagerAdapter

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun getLayoutId(): Int {
        return R.layout.fragment_edit_video
    }

    companion object {
        const val BUNDLE_VIDEO_URI = "bundle_video_uri"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()

        val uri = arguments?.getString(BUNDLE_VIDEO_URI)
        editVideoFragmentPagerAdapter = EditVideoFragmentPagerAdapter(childFragmentManager, tabLayout!!.tabCount, uri!!)
        viewPager.adapter = editVideoFragmentPagerAdapter
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
    }

    override fun setupObservers() {
    }

    override fun setupListeners() {
        tv_back.setOnClickListener {
            findNavController().popBackStack()
        }

        tv_clean.setOnClickListener {
            val editVideoRangeFragment = editVideoFragmentPagerAdapter.getFragment(0) as EditVideoRangeFragment
            val cropVideoFragment = editVideoFragmentPagerAdapter.getFragment(1) as CropVideoFragment

            editVideoRangeFragment.save()
            cropVideoFragment.save()
        }

        tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager!!.currentItem = tab!!.position
            }
        })
    }

    override fun initSettings() {
        super.initSettings()

        tv_title.text = getString(R.string.post_title)
        tv_clean.visibility = View.VISIBLE
        tv_clean.text = getString(R.string.btn_send)

    }
}