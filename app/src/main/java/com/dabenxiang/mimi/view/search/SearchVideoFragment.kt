package com.dabenxiang.mimi.view.search

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.serializable.CategoriesData
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import kotlinx.android.synthetic.main.fragment_search_video.*
import kotlinx.android.synthetic.main.fragment_search_video.iv_back
import kotlinx.android.synthetic.main.fragment_search_video.recyclerview_content
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchVideoFragment : BaseFragment<SearchVideoViewModel>() {

    companion object {
        const val KEY_DATA = "data"

        fun createBundle(id: String, title: String, isAdult: Boolean): Bundle {
            val data = CategoriesData()
            data.id = id
            data.title = title
            data.isAdult = isAdult

            return Bundle().also {
                it.putSerializable(KEY_DATA, data)
            }
        }
    }

    private val viewModel by viewModel<SearchVideoViewModel>()

    override fun fetchViewModel(): SearchVideoViewModel? {
        return viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_search_video
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (arguments?.getSerializable(KEY_DATA) as CategoriesData?)?.also { data ->

            recyclerview_content.background =
                if (data.isAdult) {
                    R.color.adult_color_background
                } else {
                    R.color.normal_color_background
                }.let {
                    requireActivity().getDrawable(it)
                }

            layout_top.background =
                if (data.isAdult) {
                    R.color.adult_color_status_bar
                } else {
                    R.color.normal_color_status_bar
                }.let {
                    requireActivity().getDrawable(it)
                }

            iv_back.setImageResource(
                if (data.isAdult) {
                    R.drawable.ic_adult_btn_back
                } else {
                    R.drawable.ic_normal_btn_back
                }
            )

            iv_search_bar.setImageResource(
                if (data.isAdult) {
                    R.drawable.bg_black_1_30_radius_18
                } else {
                    R.drawable.bg_white_1_65625_border_gray_11_radius_18
                }
            )

            iv_search.setImageResource(
                if (data.isAdult) {
                    R.drawable.ic_adult_btn_search
                } else {
                    R.drawable.ic_normal_btn_search
                }
            )

            edit_search.setTextColor(
                if (data.isAdult) {
                    R.color.adult_color_text
                } else {
                    R.color.normal_color_text
                }.let {
                    requireActivity().getColor(it)
                }
            )

            iv_clean.setImageResource(
                if (data.isAdult) {
                    R.drawable.btn_close_white
                } else {
                    R.drawable.btn_close_gray
                }
            )
        }
    }

    override fun setupObservers() {
        viewModel.searchTextLiveData.bindingEditText = edit_search

        viewModel.searchTextLiveData.observe(viewLifecycleOwner, Observer {

        })
    }

    override fun setupListeners() {
        iv_back.setOnClickListener {
            navigateTo(NavigateItem.Up)
        }

        iv_clean.setOnClickListener {
            viewModel.cleanSearchText()
        }
    }
}