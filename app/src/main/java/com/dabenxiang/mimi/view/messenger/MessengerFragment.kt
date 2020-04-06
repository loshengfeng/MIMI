package com.dabenxiang.mimi.view.messenger

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_messenger.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MessengerFragment : BaseFragment() {

    private val viewModel by viewModel<MessengerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.connect()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title.setOnClickListener {
            viewModel.publishMsg()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_messenger
    }
}