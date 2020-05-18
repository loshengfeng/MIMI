package com.dabenxiang.mimi.view.home

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.serializable.CategoriesData
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.search.SearchVideoFragment
import kotlinx.android.synthetic.main.fragment_categories.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class CategoriesFragment : BaseFragment<CategoriesViewModel>() {

    companion object {
        const val KEY_DATA = "data"

        fun createBundle(title: String): Bundle {
            val data = CategoriesData()
            data.title = title

            return Bundle().also {
                it.putSerializable(KEY_DATA, data)
            }
        }
    }

    private val viewModel by viewModel<CategoriesViewModel>()

    override fun getLayoutId(): Int {
        return R.layout.fragment_categories
    }

    override fun fetchViewModel(): CategoriesViewModel? {
        return viewModel
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isAdult = mainViewModel?.adultMode?.value ?: false

        (arguments?.getSerializable(KEY_DATA) as CategoriesData?)?.also { data ->
            tv_title.text = data.title

            recyclerview_content.background =
                if (isAdult) {
                    R.color.adult_color_background
                } else {
                    R.color.normal_color_background
                }.let { res ->
                    requireActivity().getDrawable(res)
                }

            layout_top.background =
                if (isAdult) {
                    R.color.adult_color_status_bar
                } else {
                    R.color.normal_color_status_bar
                }.let { res ->
                    requireActivity().getDrawable(res)
                }

            iv_back.setImageResource(
                if (isAdult) {
                    R.drawable.ic_adult_btn_back
                } else {
                    R.drawable.ic_normal_btn_back
                }
            )

            iv_search.setImageResource(
                if (isAdult) {
                    R.drawable.ic_adult_btn_search
                } else {
                    R.drawable.ic_normal_btn_search
                }
            )

            tv_title.setTextColor(
                if (isAdult) {
                    R.color.adult_color_text
                } else {
                    R.color.normal_color_text
                }.let { res ->
                    requireActivity().getColor(res)
                }
            )

            iv_back.setOnClickListener {
                navigateTo(NavigateItem.Up)
            }

            iv_search.setOnClickListener {
                val bundle = SearchVideoFragment.createBundle("")
                navigateTo(NavigateItem.Destination(R.id.action_categoriesFragment_to_searchVideoFragment, bundle))
            }
        }
    }

    override fun setupObservers() {
    }

    override fun setupListeners() {

        btn_collapsing_filter.setOnClickListener {
            layout_filter.visibility = View.VISIBLE
        }
    }
}