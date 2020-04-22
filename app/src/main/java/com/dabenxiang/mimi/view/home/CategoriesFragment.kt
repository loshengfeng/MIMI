package com.dabenxiang.mimi.view.home

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.serializable.CategoriesData
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import kotlinx.android.synthetic.main.fragment_categories.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class CategoriesFragment : BaseFragment<CategoriesViewModel>() {

    companion object {
        const val KEY_DATA = "data"

        fun createBundle(id: String, title: String): Bundle {
            val data = CategoriesData()
            data.id = id
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

    override fun setupObservers() {

    }

    override fun setupListeners() {
        iv_back.setOnClickListener {
            navigateTo(NavigateItem.Up)
        }

        iv_search.setOnClickListener {
            navigateTo(NavigateItem.Destination(R.id.action_categoriesFragment_to_searchVideoFragment))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (arguments?.getSerializable(KEY_DATA) as CategoriesData?)?.also {
            tv_title.text = it.title
        }
    }
}