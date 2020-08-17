package com.dabenxiang.mimi.view.orderresult

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment

class OrderResultFragment : BaseFragment() {

    private val viewModel: OrderResultViewModel by viewModels()

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_order_result
    }

    override fun setupObservers() {

    }

    override fun setupListeners() {

    }

}