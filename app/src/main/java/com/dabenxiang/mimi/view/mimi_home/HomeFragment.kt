package com.dabenxiang.mimi.view.mimi_home

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_mimi_home.*

class HomeFragment : BaseFragment() {

    private val viewModel: MiMiHomeViewModel by viewModels()

    override fun getLayoutId() = R.layout.fragment_mimi_home
    override fun setupObservers() {
        TODO("Not yet implemented")
    }

    override fun setupListeners() {
        TODO("Not yet implemented")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title.text =getString(R.string.home)
    }
}
