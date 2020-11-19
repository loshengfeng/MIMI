package com.dabenxiang.mimi.view.mimi_home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_mimi_home.*

class MiMiFragment : BaseFragment() {

    private val viewModel: MiMiViewModel by viewModels()

    override fun getLayoutId() = R.layout.fragment_mimi_home
    override fun setupObservers() {

    }

    override fun setupListeners() {

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title.text =getString(R.string.home)
    }

}
