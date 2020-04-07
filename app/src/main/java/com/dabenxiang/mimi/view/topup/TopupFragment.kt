package com.dabenxiang.mimi.view.topup

import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import timber.log.Timber

class TopupFragment : BaseFragment() {
    override fun getLayoutId(): Int {
        return R.layout.fragment_topup
    }

    override fun setupObservers() {
        Timber.d("${TopupFragment::class.java.simpleName}_setupObservers")
    }

    override fun setupListeners() {
        Timber.d("${TopupFragment::class.java.simpleName}_setupListeners")
    }
}