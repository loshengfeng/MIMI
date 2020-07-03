package com.dabenxiang.mimi.view.messenger

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_messenger.*
import timber.log.Timber

class MessengerFragment : BaseFragment<MessengerViewModel>() {
    private val viewModel: MessengerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.init()
        viewModel.connect()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title.setOnClickListener {
            //TODO: test code
            viewModel.publishMsg(MessengerViewModel.PREFIX_CHAT + "3777788128132071424", "{\"ext\":\"\",\"content\":\"Hello, ni hao ma?\",\"sendTime\":\"2020-07-02T08:35:18Z\",\"type\":0}")
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_messenger
    }

    override fun fetchViewModel(): MessengerViewModel? {
        return  viewModel
    }

    override fun setupObservers() {
        Timber.d("${MessengerFragment::class.java.simpleName}_setupObservers")
    }

    override fun setupListeners() {
        Timber.d("${MessengerFragment::class.java.simpleName}_setupListeners")
    }
}