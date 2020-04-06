package com.dabenxiang.mimi.view.personal

import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import org.koin.android.viewmodel.ext.android.viewModel


class PersonalFragment : BaseFragment() {
    private val viewModel by viewModel<PersonalViewModel>()

    override fun getLayoutId(): Int {
        return R.layout.fragment_personal
    }

    override fun setupObservers() {
        TODO("Not yet implemented")
    }

    override fun setupListeners() {
        TODO("Not yet implemented")
    }

}