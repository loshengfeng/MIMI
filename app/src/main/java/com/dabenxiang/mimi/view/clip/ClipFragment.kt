package com.dabenxiang.mimi.view.clip

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment

class ClipFragment: BaseFragment() {

    private val viewModel: ClipViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_clip
    }

    override fun setupObservers() {

    }

    override fun setupListeners() {

    }

    override fun initSettings() {

    }

}