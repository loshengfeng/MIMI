package com.dabenxiang.mimi.view.messenger

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_messenger.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class MessengerFragment : BaseFragment() {

    override fun getLayoutId() = R.layout.fragment_messenger

    val viewModel by viewModel<MessengerViewModel>()

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
}