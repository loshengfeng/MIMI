package com.dabenxiang.mimi.view.chathistory

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.adapter.ChatHistoryAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import kotlinx.android.synthetic.main.fragment_chat_history.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import org.koin.android.viewmodel.ext.android.viewModel

class ChatHistoryFragment : BaseFragment<ChatHistoryViewModel>() {
    private val viewModel by viewModel<ChatHistoryViewModel>()

//    private var chatHistoryAdapter by lazy { ChatHistoryAdapter() }

    companion object { const val NO_DATA = 0 }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int { return R.layout.fragment_chat_history }

    override fun fetchViewModel(): ChatHistoryViewModel? { return  viewModel }

    override fun setupObservers() {
        viewModel.fakeChatHistory.observe(viewLifecycleOwner, Observer {
            refreshUi(it.size)
            rv_content.adapter = ChatHistoryAdapter(it, null)
        })
    }

    override fun setupListeners() {
        View.OnClickListener { buttonView ->
            when(buttonView.id) {
                R.id.tv_back -> navigateTo(NavigateItem.Up)
            }
        }.also {
            tv_back.setOnClickListener(it)
        }
    }

    override fun initSettings() {
        super.initSettings()
        tv_title.setText(R.string.title_chat_history)
        viewModel.getFakeChatHistory()
    }

    private fun refreshUi(size: Int) {
        rv_content.visibility = when(size) {
            NO_DATA -> View.GONE
            else -> View.VISIBLE
        }

        item_no_data.visibility = when(size) {
            NO_DATA -> View.VISIBLE
            else -> View.GONE
        }
    }
}