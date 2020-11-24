package com.dabenxiang.mimi.view.club.post

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.picturedetail.PictureDetailFragment
import kotlinx.android.synthetic.main.fragment_order.*
import kotlinx.android.synthetic.main.item_setting_bar.*

class ClubTextFragment : BaseFragment() {

    private val viewModel: ClubTextDetailViewModel by viewModels()

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
        viewModel.postDetailResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Success -> {
                    val item = it.result.content
//                    textDetailAdapter?.updateContent(item!!)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })
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

        viewPager.adapter = ClubPagerAdapter2(this, memberPostItem!!)
    }
}