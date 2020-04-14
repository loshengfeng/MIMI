package com.dabenxiang.mimi.view.search

import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import kotlinx.android.synthetic.main.fragment_search_video.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchVideoFragment : BaseFragment<SearchVideoViewModel>() {

    private val viewModel by viewModel<SearchVideoViewModel>()

    override fun fetchViewModel(): SearchVideoViewModel? {
        return viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_search_video
    }

    override fun setupObservers() {
        viewModel.searchTextLiveData.bindingEditText = edit_search

        viewModel.searchTextLiveData.observe(viewLifecycleOwner, Observer {

        })
    }

    override fun setupListeners() {
        iv_back.setOnClickListener {
            viewModel.navigateTo(NavigateItem.Up)
        }

        iv_clean.setOnClickListener {
            viewModel.cleanSearchText()
        }
    }

}