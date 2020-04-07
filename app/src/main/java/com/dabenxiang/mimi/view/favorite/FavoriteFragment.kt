package com.dabenxiang.mimi.view.favorite

import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import timber.log.Timber

class FavoriteFragment  : BaseFragment() {
    override fun getLayoutId(): Int {
        return R.layout.fragment_favorite
    }

    override fun setupObservers() {
        Timber.d("${FavoriteFragment::class.java.simpleName}_setupObservers")
    }

    override fun setupListeners() {
        Timber.d("${FavoriteFragment::class.java.simpleName}_setupListeners")
    }
}