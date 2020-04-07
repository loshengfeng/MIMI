package com.dabenxiang.mimi.view.personal

import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber


class PersonalFragment : BaseFragment() {
    private val viewModel by viewModel<PersonalViewModel>()

    override fun getLayoutId(): Int {
        return R.layout.fragment_personal
    }

    override fun setupObservers() {
        Timber.d("${PersonalFragment::class.java.simpleName}_setupObservers")
    }

    override fun setupListeners() {
        Timber.d("${PersonalFragment::class.java.simpleName}_setupListeners")
    }
}