package com.dabenxiang.mimi.view.chathistory

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.adapter.ChatHistoryAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_chat_history.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber

class ChatHistoryFragment : BaseFragment() {
    private val viewModel by viewModel<ChatHistoryViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getFakeChatHistory()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_chat_content
    }

    override fun setupObservers() {
        Timber.d("${ChatHistoryFragment::class.java.simpleName}_setupObservers")
        viewModel.fakeChatHistory.observe(viewLifecycleOwner, Observer {
            recyclerHistory.adapter = ChatHistoryAdapter(it, null)
        })
    }

    override fun setupListeners() {
        Timber.d("${ChatHistoryFragment::class.java.simpleName}_setupListeners")

        btnClose.setOnClickListener {
            Navigation.findNavController(view!!).navigateUp()
        }
        recyclerHistory.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }
}